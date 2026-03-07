package com.revhire.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController manages requests for the application landing page.
 *
 * Responsibilities:
 * - Display the public landing page for unauthenticated users.
 * - Redirect authenticated users to their respective dashboards.
 *
 * The redirection is based on the user's role stored in the session.
 */
@Controller
public class HomeController {

    /**
     * Logger used to track access and redirection events.
     */
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    /**
     * Handles requests to the home page or index page.
     *
     * Steps performed:
     * - Check if the user is authenticated.
     * - Retrieve the user's role from the session.
     * - Redirect employers to the employer dashboard.
     * - Redirect job seekers to the job seeker dashboard.
     * - If the user is not authenticated, show the landing page.
     *
     * @param auth Spring Security authentication object
     * @param session HTTP session used to retrieve stored user information
     * @return landing page or redirect to appropriate dashboard
     */
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