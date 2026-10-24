package org.example.amaizon.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.amaizon.model.dtos.CheckoutRequest;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.embeddable.all.AddressSnapshot;
import org.example.amaizon.model.entity.*;
import org.example.amaizon.model.enums.Carrier;

import org.example.amaizon.security.CustomUserDetails;
import org.example.amaizon.services.CartService;
import org.example.amaizon.services.CheckoutService;
import org.example.amaizon.web.GuestTokenCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final CheckoutService checkoutService;

    public CheckoutController(CartService cartService, CheckoutService checkoutService) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model,
                               HttpServletRequest req,
                               HttpServletResponse res,
                               @ModelAttribute("market") MarketContextDto market,
                               @AuthenticationPrincipal CustomUserDetails principal) {


        addCheckoutUiFlags(model, market);

        Map<Carrier, BigDecimal> shippingCostByCarrier = addShippingOptions(model);

        User user = (principal != null) ? principal.getUser() : null;
        Cart cart = cartService.getOrCreateCart(market, user, req, res);


        if (user == null && cart != null && cart.isCheckedOut()) {
            GuestTokenCookie.clear(res);
            cart = cartService.getOrCreateCart(market, null, req, res);
        }

        model.addAttribute("cart", cart);

        CheckoutRequest form = new CheckoutRequest();
        applyMarketCountry(form, market);
        model.addAttribute("checkout", form);

        addOrderSummary(model, cart, null, shippingCostByCarrier);

        return "checkout/index";
    }


    @PostMapping("/checkout/recalc")
    public String recalc(@ModelAttribute("checkout") CheckoutRequest form,
                         Model model,
                         HttpServletRequest req,
                         HttpServletResponse res,
                         @ModelAttribute("market") MarketContextDto market,
                         @AuthenticationPrincipal CustomUserDetails principal) {


        addCheckoutUiFlags(model, market);

        Map<Carrier, BigDecimal> shippingCostByCarrier = addShippingOptions(model);

        User user = (principal != null) ? principal.getUser() : null;
        Cart cart = cartService.getOrCreateCart(market, user, req, res);
        model.addAttribute("cart", cart);

        applyMarketCountry(form, market);

        addOrderSummary(model, cart, form.getShippingCarrier(), shippingCostByCarrier);

        return "checkout/index";
    }


    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("checkout") CheckoutRequest form,
                           BindingResult br,
                           Model model,
                           HttpServletRequest req,
                           HttpServletResponse res,
                           @ModelAttribute("market") MarketContextDto market,
                           @AuthenticationPrincipal CustomUserDetails principal) {


        addCheckoutUiFlags(model, market);

        Map<Carrier, BigDecimal> shippingCostByCarrier = addShippingOptions(model);

        User user = (principal != null) ? principal.getUser() : null;
        Cart cart = cartService.getOrCreateCart(market, user, req, res);
        model.addAttribute("cart", cart);

        applyMarketCountry(form, market);


        addOrderSummary(model, cart, form.getShippingCarrier(), shippingCostByCarrier);

        if (br.hasErrors()) {
            return "checkout/index";
        }

        AddressSnapshot shipping = form.toShippingSnapshot();
        AddressSnapshot billing = form.toBillingSnapshot();

        Order order = checkoutService.checkout(
                cart, user,
                form.getContactEmail(),
                shipping, billing,
                form.getBillingCustomerType(),
                form.getBillingCompanyName(),
                form.getBillingTaxId(),
                form.getShippingCarrier()
        );

        if (user == null) {
            req.getSession(true).setAttribute(
                    "AMAIZON_GUEST_ORDER_" + order.getStableId(),
                    Boolean.TRUE
            );
            GuestTokenCookie.clear(res);
        }

        return "redirect:/checkout/success/" + order.getStableId();
    }

    @GetMapping("/checkout/success/{stableId}")
    public String success(@PathVariable UUID stableId, Model model) {
        model.addAttribute("orderStableId", stableId);
        return "checkout/success";
    }


    private static void applyMarketCountry(CheckoutRequest form, MarketContextDto market) {
        String iso = market.getCountry().getCode().getValue();
        String name = market.getCountry().getName().getValue();

        form.setShippingCountryCode(iso);
        form.setShippingCountryName(name);
        form.setBillingCountryCode(iso);
        form.setBillingCountryName(name);
    }

    private static void
    addCheckoutUiFlags(Model model, MarketContextDto market) {
        String iso = market.getCountry().getCode().getValue();
        boolean isUS = "US".equalsIgnoreCase(iso);

        model.addAttribute("showState", isUS);
        model.addAttribute("showPostal", true);
        model.addAttribute("postalLabelKey", isUS ? "checkout.zip" : "checkout.postal");
    }

    private static Map<Carrier, BigDecimal> addShippingOptions(Model model) {
        Map<Carrier, BigDecimal> map = new EnumMap<>(Carrier.class);
        map.put(Carrier.INPOST, new BigDecimal("12.99"));
        map.put(Carrier.DPD, new BigDecimal("19.99"));
        map.put(Carrier.DHL, new BigDecimal("24.99"));
        map.put(Carrier.GLS, new BigDecimal("18.99"));
        model.addAttribute("shippingCostByCarrier", map);
        return map;
    }

    private static void addOrderSummary(Model model,
                                        Cart cart,
                                        Carrier selectedCarrier,
                                        Map<Carrier, BigDecimal> shippingCostByCarrier) {

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQty = 0;

        if (cart != null && cart.getItems() != null) {
            for (CartItem it : cart.getItems()) {
                int qty = it.getQuantity();
                totalQty += qty;

                BigDecimal unit = unitPriceOrZero(it);
                subtotal = subtotal.add(unit.multiply(BigDecimal.valueOf(qty)));
            }
        }

        BigDecimal shipping = BigDecimal.ZERO;
        boolean carrierSelected = (selectedCarrier != null);

        if (carrierSelected && shippingCostByCarrier != null) {
            shipping = shippingCostByCarrier.getOrDefault(selectedCarrier, BigDecimal.ZERO);
        }

        BigDecimal total = subtotal.add(shipping);

        model.addAttribute("summarySubtotal", subtotal);
        model.addAttribute("summaryShipping", shipping);
        model.addAttribute("summaryTotal", total);
        model.addAttribute("summaryQty", totalQty);
        model.addAttribute("summaryCarrierSelected", carrierSelected);
    }

    private static BigDecimal unitPriceOrZero(CartItem it) {
        if (it == null) return BigDecimal.ZERO;
        ProductCountry pc = it.getProductCountry();
        if (pc == null) return BigDecimal.ZERO;

        ProductPrice pp = pc.getPrice();
        if (pp == null) return BigDecimal.ZERO;

        BigDecimal v = pp.getPriceValue();
        return (v != null) ? v : BigDecimal.ZERO;
    }

}
