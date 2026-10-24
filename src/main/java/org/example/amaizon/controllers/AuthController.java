package org.example.amaizon.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/staff/login")
    public String loginStaff() {
        return "staff/login-staff";
    }

    @GetMapping("/staff/logout")
    public String logoutStaff() {
        return "staff/logout-staff";
    }


}
