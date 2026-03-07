package com.revhire.controller;

import com.revhire.model.User;
import com.revhire.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * DashboardController handles redirection to the appropriate dashboard
 * after a user successfully logs into the system.
 *
 * Responsibilities:
 * - Identify the logged-in user using Spring Security authentication.
 * - Retrieve user details from the database.
 * - Store user information in the HTTP session.
 * - Redirect users to their respective dashboards based on role.
 */
@Controller
public class DashboardController {

    /**
     * UserService provides operations related to user data retrieval.
     */
    private final UserService userService;

    /**
     * Constructor used for dependency injection.
     *
     * @param userService service responsible for user-related operations
     */
    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles dashboard redirection after login.
     *
     * Steps performed:
     * - Retrieve authenticated user's email from Spring Security.
     * - Fetch the user details from the database.
     * - Store user information in HTTP session attributes.
     * - Redirect the user to the appropriate dashboard based on role.
     *
     * Job seekers are redirected to the job seeker dashboard.
     * Employers are redirected to the employer dashboard.
     *
     * If the user is not authenticated, the request is redirected to the login page.
     *
     * @param model Spring model used to pass data to the view
     * @param authentication authentication object provided by Spring Security
     * @param session HTTP session used to store user information
     * @return redirect URL for the appropriate dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication, HttpSession session) {
        if (authentication != null) {
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getFullName());
            session.setAttribute("userRole", user.getRole().name());
            
            model.addAttribute("user", user);
            
            // Redirect based on role
            if (user.getRole() == User.Role.JOBSEEKER) {
                return "redirect:/jobseeker/dashboard";
            } else if (user.getRole() == User.Role.EMPLOYER) {
                return "redirect:/employer/dashboard";
            }
        }
        return "redirect:/auth/login";
    }
}