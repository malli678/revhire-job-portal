package com.revhire.controller;

import com.revhire.dto.ForgotPasswordDto;
import com.revhire.dto.PasswordChangeDto;
import com.revhire.dto.ResetPasswordDto;
import com.revhire.service.PasswordService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class PasswordController {

	private final PasswordService passwordService;

	public PasswordController(PasswordService passwordService) {
		this.passwordService = passwordService;
	}

	// Show change password page (for logged in users)
	@GetMapping("/change-password")
	public String showChangePasswordPage(Model model, Authentication authentication) {
		if (authentication == null) {
			return "redirect:/auth/login";
		}
		return "auth/change-password";
	}

	// Process change password
	@PostMapping("/change-password")
	public String changePassword(@ModelAttribute PasswordChangeDto dto, Authentication authentication, Model model) {
		try {
			Long userId = (Long) authentication.getDetails();
			passwordService.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword());
			model.addAttribute("success", "Password changed successfully!");
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
		}
		return "auth/change-password";
	}

	// Show forgot password page
	@GetMapping("/forgot-password")
	public String showForgotPasswordPage() {
		return "auth/forgot-password";
	}

	// Process forgot password (send reset link)
	@PostMapping("/forgot-password")
	public String processForgotPassword(@ModelAttribute ForgotPasswordDto dto, Model model) {
		try {
			String token = passwordService.generateResetToken(dto.getEmail());
			// In production, send email here
			// For now, we'll show the token
			model.addAttribute("token", token);
			model.addAttribute("email", dto.getEmail());
			model.addAttribute("success", "Password reset link generated!");
			return "auth/reset-password";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "auth/forgot-password";
		}
	}

	// Show reset password page (with token)
	@GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam String token, Model model) {
        if (!passwordService.validateToken(token)) {
            model.addAttribute("error", "Invalid or expired reset link");
            return "auth/forgot-password";
            		
        } model.addAttribute("token", token); 
        return "auth/reset-password"; 
        }

	// Process reset password
	@PostMapping("/reset-password")
	public String processResetPassword(@ModelAttribute ResetPasswordDto dto, Model model) {
		try {
			if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
				model.addAttribute("error", "Passwords do not match");
				model.addAttribute("token", dto.getToken());
				return "auth/reset-password";
			}

			passwordService.resetPassword(dto.getToken(), dto.getNewPassword());
			model.addAttribute("success", "Password reset successfully! You can now login.");
			return "auth/login";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("token", dto.getToken());
			return "auth/reset-password";
		}
	}
}