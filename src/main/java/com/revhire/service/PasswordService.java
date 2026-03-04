package com.revhire.service;

import com.revhire.model.PasswordResetToken;
import com.revhire.model.User;
import com.revhire.repository.PasswordResetTokenRepository;
import com.revhire.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public PasswordService(UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password cannot be the same as the current password");
        }

        validatePasswordStrength(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Trigger Security Notification
        notificationService.createNotification(
                user.getUserId(),
                "Security Alert: Password Changed",
                "Your account password was successfully updated. If you did not authorize this change, please recover your account immediately.",
                "SECURITY",
                null);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Invalid reset token");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token has expired");
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password cannot be the same as the previous password");
        }

        validatePasswordStrength(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);

        // Trigger Security Notification
        notificationService.createNotification(
                user.getUserId(),
                "Security Alert: Password Reset Successful",
                "Your account password was successfully reset using the forgot password flow. If you did not authorize this change, please contact support immediately.",
                "SECURITY",
                null);
    }

    @Transactional
    public String generateResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        User user = userOpt.get();

        // IMPORTANT: Delete ANY existing tokens for this user FIRST
        tokenRepository.findByUserUserId(user.getUserId()).ifPresent(existingToken -> {
            tokenRepository.delete(existingToken);
            tokenRepository.flush(); // Force immediate deletion
        });

        // Generate new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(expiryDate);

        tokenRepository.save(resetToken);

        // For testing, print token to console
        System.out.println("🔑 PASSWORD RESET TOKEN for " + email + ": " + token);

        return token;
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        return !tokenOpt.get().isExpired();
    }

    public String getSecurityQuestion(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String question = user.getSecurityQuestion();
        if (question == null || question.trim().isEmpty()) {
            throw new RuntimeException(
                    "No security question is set for this account. Please login and update your profile.");
        }
        return question;
    }

    public boolean verifySecurityAnswer(String email, String answer) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Case-insensitive comparison
        if (user.getSecurityAnswer() == null)
            return false;
        return user.getSecurityAnswer().equalsIgnoreCase(answer.trim());
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            throw new RuntimeException("Password must be between 8 and 20 characters");
        }
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$")) {
            throw new RuntimeException(
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)");
        }
    }
}