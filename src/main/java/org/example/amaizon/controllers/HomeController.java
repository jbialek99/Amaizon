package org.example.amaizon.controllers;

import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.dtos.ProductDto;
import org.example.amaizon.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/", "/home"})
    public String home(@ModelAttribute("market") MarketContextDto market,
                       Model model) {

        UUID countryStableId = market.getCountry().getStableId();

        List<ProductDto> products =
                productService.findHomeProducts(countryStableId);

        model.addAttribute("products", products);
        model.addAttribute("noProducts", products.isEmpty());

        return "home";
    }

    @GetMapping("/home-staff")
    public String homeStaff() {
        return "staff/home-staff";
    }
}
