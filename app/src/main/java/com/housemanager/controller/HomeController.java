package com.housemanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHomePage(Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {

            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            if (roles.contains("ROLE_ADMIN")) {
                return "redirect:/admin";
            } else if (roles.contains("ROLE_COMPANY")) {
                return "redirect:/company";
            } else if (roles.contains("ROLE_EMPLOYEE")) {
                return "redirect:/employee";
            } else if (roles.contains("ROLE_RESIDENT")) {
                return "redirect:/resident";
            }
        }

        return "index";
    }
}