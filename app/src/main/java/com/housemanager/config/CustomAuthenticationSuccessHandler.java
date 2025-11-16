package com.housemanager.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String targetUrl;
        if (roles.contains("ROLE_ADMIN")) {
            targetUrl = "/admin";
        } else if (roles.contains("ROLE_COMPANY")) {
            targetUrl = "/company";
        } else if (roles.contains("ROLE_EMPLOYEE")) {
            targetUrl = "/employee";
        } else if (roles.contains("ROLE_RESIDENT")) {
            targetUrl = "/resident";
        } else {
            targetUrl = "/";
        }

        response.sendRedirect(targetUrl);
    }
}