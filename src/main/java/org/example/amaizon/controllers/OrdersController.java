package org.example.amaizon.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.amaizon.model.entity.Order;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.repositories.OrderRepository;
import org.example.amaizon.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    private final OrderRepository orderRepository;

    public OrdersController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/{stableId}")
    public String details(
            @PathVariable UUID stableId,
            Model model,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Order order = orderRepository.findDetailsByStableId(stableId)
                .orElseThrow(OrdersController::notFound);

        if (!canRead(order, request, principal)) {
            throw notFound();
        }

        model.addAttribute("order", order);
        return "orders/details";
    }

    private static boolean canRead(
            Order order,
            HttpServletRequest request,
            CustomUserDetails principal
    ) {
        User owner = order.getUser();

        if (principal != null) {
            User currentUser = principal.getUser();

            return currentUser != null
                    && owner != null
                    && currentUser.getId() == owner.getId();
        }

        if (owner != null) {
            return false;
        }

        HttpSession session = request.getSession(false);

        return session != null
                && Boolean.TRUE.equals(
                    session.getAttribute(
                        "AMAIZON_GUEST_ORDER_" + order.getStableId()
                    )
                );
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
