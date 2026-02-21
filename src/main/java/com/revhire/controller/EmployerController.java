package com.revhire.controller;

import com.revhire.model.User;
import com.revhire.service.UserService;
import com.revhire.service.JobService;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployerController {

    private final UserService userService;
    private final JobService jobService;

    // Constructor Injection
    public EmployerController(UserService userService,
                              JobService jobService) {
        this.userService = userService;
        this.jobService = jobService;
    }

    // Employer Dashboard
    @GetMapping("/employer/dashboard")
    public String dashboard(Model model,
                            Authentication authentication,
                            HttpSession session) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        if (user == null) {
            return "redirect:/auth/login";
        }

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());

        model.addAttribute("user", user);

        return "employer/dashboard";
    }

    // Open Post Job Page
    @GetMapping("/employer/post-job")
    public String postJobPage() {
        return "employer/post-job";
    }

    // Manage Jobs Page
    @GetMapping("/employer/manage-jobs")
    public String manageJobs(Model model) {

        model.addAttribute("jobs", jobService.getAllJobs());

        return "employer/manage-jobs";
    }
}