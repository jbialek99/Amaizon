package org.example.amaizon.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.amaizon.handlers.GlobalExceptionHandler;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.dtos.RegisterRequest;
import org.example.amaizon.services.UserRegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

    private final UserRegistrationService registrationService;

    public RegistrationController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterRequest());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("form") RegisterRequest form,
            @ModelAttribute("market") MarketContextDto market,
            BindingResult bindingResult,
            HttpServletRequest request
    ) {
        request.setAttribute(GlobalExceptionHandler.FORM_VIEW_ATTR, "auth/register");

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        request.setAttribute("form", form);

        registrationService.register(market, form);

        return "redirect:/login?registered";
    }
}
