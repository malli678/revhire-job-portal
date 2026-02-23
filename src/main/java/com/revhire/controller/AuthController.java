package com.revhire.controller;

import com.revhire.dto.EmployerRegistrationDto;
import com.revhire.dto.JobSeekerRegistrationDto;
import com.revhire.dto.LoginDto;
import com.revhire.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ✅ ONLY show login page
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginDto", new LoginDto());
        return "auth/login";
    }

    // ❌ REMOVED manual login method
    // Spring Security handles POST /auth/login

    @GetMapping("/register/seeker")
    public String showJobSeekerRegistrationForm(Model model) {
        model.addAttribute("registrationDto", new JobSeekerRegistrationDto());
        return "auth/register-seeker";
    }

    @PostMapping("/register/seeker")
    public String registerJobSeeker(@Valid @ModelAttribute JobSeekerRegistrationDto registrationDto,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) return "auth/register-seeker";

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
            return "auth/register-seeker";
        }

        try {
            userService.registerJobSeeker(registrationDto);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register/seeker";
        }
    }

    @GetMapping("/register/employer")
    public String showEmployerRegistrationForm(Model model) {
        model.addAttribute("registrationDto", new EmployerRegistrationDto());
        return "auth/register-employer";
    }

    @PostMapping("/register/employer")
    public String registerEmployer(@Valid @ModelAttribute EmployerRegistrationDto registrationDto,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) return "auth/register-employer";

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
            return "auth/register-employer";
        }

        try {
            userService.registerEmployer(registrationDto);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register/employer";
        }
    }
}