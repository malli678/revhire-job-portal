package com.revhire.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @GetMapping({ "/", "/index" })
    public String home(org.springframework.security.core.Authentication auth,
            jakarta.servlet.http.HttpSession session) {
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String role = (String) session.getAttribute("userRole");
            if ("EMPLOYER".equals(role)) {
                log.info("Redirecting authenticated employer to dashboard");
                return "redirect:/employer/dashboard";
            } else if ("JOBSEEKER".equals(role)) {
                log.info("Redirecting authenticated jobseeker to dashboard");
                return "redirect:/jobseeker/dashboard";
            }
        }

        log.info("Accessed landing page (/)");
        return "index";
    }
}
