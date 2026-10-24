package org.example.amaizon.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.model.dtos.CartDto;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.Cart;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.security.CustomUserDetails;
import org.example.amaizon.services.CartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String cart(Model model,
                       @ModelAttribute("market") MarketContextDto market,
                       HttpServletRequest req,
                       HttpServletResponse res,
                       @AuthenticationPrincipal CustomUserDetails principal) {

        User user = principal != null ? principal.getUser() : null;

        Cart cart = cartService.getOrCreateCart(market, user, req, res);
        CartDto cartDto = cartService.toDto(cart);

        model.addAttribute("cart", cartDto);

        return "cart/index";
    }

    @PostMapping("/cart/add")
    public String add(@RequestParam("productCountryStableId") UUID productCountryStableId,
                      @RequestParam("qty") int qty,
                      @ModelAttribute("market") MarketContextDto market,
                      HttpServletRequest req,
                      HttpServletResponse res,
                      @AuthenticationPrincipal CustomUserDetails principal) {

        User user = principal != null ? principal.getUser() : null;

        cartService.addToCart(
                market,
                user,
                req,
                res,
                productCountryStableId,
                qty
        );

        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String update(@RequestParam("productCountryStableId") UUID productCountryStableId,
                         @RequestParam("qty") int qty,
                         @ModelAttribute("market") MarketContextDto market,
                         HttpServletRequest req,
                         HttpServletResponse res,
                         @AuthenticationPrincipal CustomUserDetails principal) {

        User user = principal != null ? principal.getUser() : null;

        cartService.updateQty(
                market,
                user,
                req,
                res,
                productCountryStableId,
                qty
        );

        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String remove(@RequestParam("productCountryStableId") UUID productCountryStableId,
                         @ModelAttribute("market") MarketContextDto market,
                         HttpServletRequest req,
                         HttpServletResponse res,
                         @AuthenticationPrincipal CustomUserDetails principal) {

        User user = principal != null ? principal.getUser() : null;

        cartService.removeItem(
                market,
                user,
                req,
                res,
                productCountryStableId
        );

        return "redirect:/cart";
    }
}
