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

/**
 * AuthController handles authentication related operations in the application.
 *
 * Responsibilities:
 * - Display login page.
 * - Register new job seekers.
 * - Register new employers.
 * - Handle validation errors during registration.
 * - Show access denied page.
 *
 * This controller works together with Spring Security
 * to manage authentication and user registration.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    /**
     * Logger used to track authentication related events and errors.
     */
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /**
     * UserService handles business logic related to user registration.
     */
    private final UserService userService;

    /**
     * Constructor for dependency injection.
     *
     * @param userService service responsible for user-related operations
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ===================== LOGIN =====================

    /**
     * Displays the login form.
     *
     * If the login DTO is not already present in the model,
     * a new LoginDto object is created and added to the model.
     *
     * @param model Spring model used to pass data to the view
     * @return login page
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (!model.containsAttribute("loginDto")) {
            model.addAttribute("loginDto", new LoginDto());
        }
        return "auth/login";
    }

    // Spring Security handles POST /auth/login

    // ===================== JOB SEEKER REGISTRATION =====================

    /**
     * Displays the job seeker registration form.
     *
     * @param model Spring model used to pass registration data
     * @return job seeker registration page
     */
    @GetMapping("/register/seeker")
    public String showJobSeekerRegistrationForm(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new JobSeekerRegistrationDto());
        }
        return "auth/register-seeker";
    }

    /**
     * Handles job seeker registration.
     *
     * Steps performed:
     * - Validate form fields.
     * - Convert employment status to uppercase.
     * - Check password confirmation.
     * - Register the job seeker using UserService.
     * - Handle validation and system errors.
     *
     * @param registrationDto job seeker registration data
     * @param result validation result
     * @param model Spring model
     * @param redirectAttributes used to pass success messages
     * @return redirect to login page if successful
     */
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
            result.reject("error.registration", e.getMessage());
            return "auth/register-seeker";

        } catch (Exception e) {
            log.error("Unexpected registration error: ", e);
            result.reject("error.registration", "Something went wrong. Please try again.");
            return "auth/register-seeker";
        }
    }

    // ===================== EMPLOYER REGISTRATION =====================

    /**
     * Displays the employer registration form.
     *
     * @param model Spring model used to pass registration data
     * @return employer registration page
     */
    @GetMapping("/register/employer")
    public String showEmployerRegistrationForm(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new EmployerRegistrationDto());
        }
        return "auth/register-employer";
    }

    /**
     * Handles employer registration.
     *
     * Steps performed:
     * - Validate form fields.
     * - Check password confirmation.
     * - Register employer using UserService.
     * - Handle validation and system errors.
     *
     * @param registrationDto employer registration data
     * @param result validation result
     * @param model Spring model
     * @param redirectAttributes used to pass success messages
     * @return redirect to login page if successful
     */
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

    /**
     * Displays access denied page when user tries to access restricted resources.
     *
     * @return access denied page
     */
    @GetMapping("/access-denied")
    public String showAccessDenied() {
        return "auth/access-denied";
    }
}