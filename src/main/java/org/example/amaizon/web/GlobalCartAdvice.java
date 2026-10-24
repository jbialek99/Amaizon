package org.example.amaizon.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.Cart;
import org.example.amaizon.model.entity.CartItem;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.resolvers.MarketResolver;
import org.example.amaizon.security.CustomUserDetails;
import org.example.amaizon.services.CartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalCartAdvice {

    private final CartService cartService;

    public GlobalCartAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount(@AuthenticationPrincipal CustomUserDetails principal,
                             HttpServletRequest req,
                             HttpServletResponse res) {
        try {
            Object marketRequest = req.getAttribute(MarketResolver.REQ_ATTR);
            if (!(marketRequest instanceof MarketContextDto market)) {
                return 0;
            }
            User user = (principal != null) ? principal.getUser() : null;
            Cart cart = cartService.getOrCreateCart(market, user, req, res);
            if (cart == null || cart.getItems() == null) return 0;

            return cart.getItems().stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        } catch (Exception e) {
            return 0;
        }
    }
}
