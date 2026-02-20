package com.revhire.controller;

import com.revhire.model.User;
import com.revhire.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JobSeekerController {

    private final UserService userService;

    public JobSeekerController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/jobseeker/dashboard")
    public String dashboard(Model model, Authentication authentication, HttpSession session) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());
        
        model.addAttribute("user", user);
        
        return "jobseeker/dashboard";
    }
}