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

    // ===================== LOGIN =====================

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (!model.containsAttribute("loginDto")) {
            model.addAttribute("loginDto", new LoginDto());
        }
        return "auth/login";
    }

    // Spring Security handles POST /auth/login

    // ===================== JOB SEEKER REGISTRATION =====================

    @GetMapping("/register/seeker")
    public String showJobSeekerRegistrationForm(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new JobSeekerRegistrationDto());
        }
        return "auth/register-seeker";
    }

    @PostMapping("/register/seeker")
    public String registerJobSeeker(
            @Valid @ModelAttribute("registrationDto") JobSeekerRegistrationDto registrationDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Convert employment status to uppercase
        if (registrationDto.getCurrentEmploymentStatus() != null) {
            registrationDto.setCurrentEmploymentStatus(
                    registrationDto.getCurrentEmploymentStatus().toUpperCase());
        }

        // Field-level validation errors
        if (result.hasErrors()) {
            return "auth/register-seeker";
        }

        // Password match validation
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
            return "auth/register-seeker";
        }

        try {
            userService.registerJobSeeker(registrationDto);
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please login.");
            return "redirect:/auth/login";

        } catch (IllegalArgumentException e) {
            log.warn("Business validation failed: {}", e.getMessage());
            // Add error to the model
            result.reject("error.registration", e.getMessage());
            return "auth/register-seeker"; // Return to same page with errors

        } catch (Exception e) {
            log.error("Unexpected registration error: ", e);
            result.reject("error.registration", "Something went wrong. Please try again.");
            return "auth/register-seeker";
        }
    }
    // ===================== EMPLOYER REGISTRATION =====================

    @GetMapping("/register/employer")
    public String showEmployerRegistrationForm(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new EmployerRegistrationDto());
        }
        return "auth/register-employer";
    }

    @PostMapping("/register/employer")
    public String registerEmployer(
            @Valid @ModelAttribute("registrationDto") EmployerRegistrationDto registrationDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Debug: print all errors
        if (result.hasErrors()) {
            System.out.println("=== VALIDATION ERRORS ===");
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
        }

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
            return "auth/register-employer";
        }

        try {
            userService.registerEmployer(registrationDto);
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please login.");
            return "redirect:/auth/login";

        } catch (IllegalArgumentException e) {
            log.warn("Business validation failed: {}", e.getMessage());
            result.reject("error.registration", e.getMessage());
            return "auth/register-employer";

        } catch (Exception e) {
            log.error("Unexpected registration error: ", e);
            result.reject("error.registration", "Something went wrong. Please try again.");
            return "auth/register-employer";
        }
    }

    @GetMapping("/access-denied")
    public String showAccessDenied() {
        return "auth/access-denied";
    }
}