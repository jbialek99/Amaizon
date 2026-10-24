package org.example.amaizon.controllers;

import org.example.amaizon.model.dtos.MarketContextDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class AboutController {

    @GetMapping("/about")
    public String about(@ModelAttribute("market") MarketContextDto market) {
        return "about";
    }
}
