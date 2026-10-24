package org.example.amaizon.controllers;

import org.example.amaizon.model.entity.Order;
import org.example.amaizon.repositories.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
public class OrdersController {

    private final OrderRepository orderRepository;

    public OrdersController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/orders/{stableId}")
    public String details(@PathVariable UUID stableId, Model model) {

        Order order = orderRepository.findDetailsByStableId(stableId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + stableId));

        model.addAttribute("order", order);
        return "orders/details";
    }
}
