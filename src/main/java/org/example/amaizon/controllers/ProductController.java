package org.example.amaizon.controllers;

import org.example.amaizon.model.dtos.ProductDto;
import org.example.amaizon.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/p/{id}")
    public String details(@PathVariable("id") UUID productCountryStableId,
                          Model model) {

        ProductDto product = productService
                .findProductDetailsById(productCountryStableId)
                .orElse(null);

        model.addAttribute("product", product);

        return "product/details";
    }
}
