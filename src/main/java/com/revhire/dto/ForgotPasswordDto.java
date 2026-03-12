package com.revhire.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * ForgotPasswordDto is a Data Transfer Object used during
 * the forgot password process.
 *
 * It captures the user's email address so the system can
 * verify the user and retrieve the associated security question
 * or initiate the password reset process.
 */
public class ForgotPasswordDto {

    /**
     * Email address of the user requesting password reset.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Getters and Setters

    /**
     * Returns the user's email address.
     *
     * @return email
     */
    public String getEmail() { return email; }

    /**
     * Sets the user's email address.
     *
     * @param email user email
     */
    public void setEmail(String email) { this.email = email; }
}