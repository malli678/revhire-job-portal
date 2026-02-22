package com.revhire.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordUtil {
    
    private static final SecureRandom random = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;
    
    /**
     * Generate a random temporary password
     */
    public static String generateTemporaryPassword() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, 10);
    }
    
    /**
     * Validate password strength
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasDigit = false;
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        
        return hasDigit && hasLower && hasUpper && hasSpecial;
    }
    
    /**
     * Generate password reset token
     */
    public static String generateResetToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}