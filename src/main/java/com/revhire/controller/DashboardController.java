package com.revhire.controller;

import com.revhire.model.User;
import com.revhire.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

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