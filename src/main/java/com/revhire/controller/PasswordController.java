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

/**
 * PasswordController manages all password related operations.
 *
 * Responsibilities:
 * - Allow logged-in users to change their password.
 * - Handle forgot password workflow.
 * - Verify security questions.
 * - Generate password reset tokens.
 * - Reset user passwords securely.
 */
@Controller
@RequestMapping("/auth")
public class PasswordController {

	private final PasswordService passwordService;

	/**
	 * Constructor for dependency injection.
	 *
	 * @param passwordService service responsible for password operations
	 */
	public PasswordController(PasswordService passwordService) {
		this.passwordService = passwordService;
	}

	/**
	 * Displays the change password page for authenticated users.
	 *
	 * @param model Spring model
	 * @param authentication authentication object of logged-in user
	 * @return change password page or redirect to login if user not authenticated
	 */
	@GetMapping("/change-password")
	public String showChangePasswordPage(Model model, Authentication authentication) {
		if (authentication == null) {
			return "redirect:/auth/login";
		}
		return "auth/change-password";
	}

	/**
	 * Processes the change password request.
	 *
	 * @param dto password change data
	 * @param authentication authenticated user information
	 * @param model Spring model used to display messages
	 * @return change password page with success or error message
	 */
	@PostMapping("/change-password")
	public String changePassword(@ModelAttribute PasswordChangeDto dto, Authentication authentication, Model model) {
		try {
			String email = authentication.getName();
			passwordService.changePassword(email, dto.getCurrentPassword(), dto.getNewPassword());
			model.addAttribute("success", "Password changed successfully!");
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
		}
		return "auth/change-password";
	}

	/**
	 * Displays the forgot password page.
	 *
	 * @return forgot password page
	 */
	@GetMapping("/forgot-password")
	public String showForgotPasswordPage() {
		return "auth/forgot-password";
	}

	/**
	 * Processes forgot password request and retrieves the security question.
	 *
	 * @param dto forgot password request data
	 * @param model Spring model used to pass security question
	 * @return security question page or forgot password page if error occurs
	 */
	@PostMapping("/forgot-password")
	public String processForgotPassword(@ModelAttribute ForgotPasswordDto dto, Model model) {
		try {
			String question = passwordService.getSecurityQuestion(dto.getEmail());
			model.addAttribute("email", dto.getEmail());
			model.addAttribute("securityQuestion", question);
			return "auth/security-question";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "auth/forgot-password";
		}
	}

	/**
	 * Verifies the security answer provided by the user.
	 * If correct, generates a password reset token.
	 *
	 * @param email user email
	 * @param answer security question answer
	 * @param model Spring model used to pass reset token
	 * @return reset password page or security question page if verification fails
	 */
	@PostMapping("/verify-security-answer")
	public String verifySecurityAnswer(@RequestParam String email, @RequestParam String answer, Model model) {
		try {
			if (passwordService.verifySecurityAnswer(email, answer)) {
				String token = passwordService.generateResetToken(email);
				model.addAttribute("token", token);
				return "auth/reset-password";
			} else {
				model.addAttribute("error", "Incorrect answer to security question.");
				model.addAttribute("email", email);
				model.addAttribute("securityQuestion", passwordService.getSecurityQuestion(email));
				return "auth/security-question";
			}
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "auth/forgot-password";
		}
	}

	/**
	 * Displays reset password page using the reset token.
	 *
	 * @param token password reset token
	 * @param model Spring model used to pass token
	 * @return reset password page or forgot password page if token invalid
	 */
	@GetMapping("/reset-password")
	public String showResetPasswordPage(@RequestParam String token, Model model) {
		if (!passwordService.validateToken(token)) {
			model.addAttribute("error", "Invalid or expired reset link");
			return "auth/forgot-password";

		}
		model.addAttribute("token", token);
		return "auth/reset-password";
	}

	/**
	 * Processes the reset password request.
	 *
	 * @param dto reset password data
	 * @param model Spring model used to display messages
	 * @return login page after successful reset or reset password page if error occurs
	 */
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