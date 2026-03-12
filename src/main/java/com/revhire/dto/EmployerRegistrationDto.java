package com.revhire.dto;

import jakarta.validation.constraints.*;

/**
 * EmployerRegistrationDto is a Data Transfer Object used to capture
 * employer registration details from the registration form.
 *
 * It includes validation rules to ensure that all required fields
 * are properly formatted before creating a new employer account.
 *
 * This DTO transfers employer registration data from the
 * presentation layer to the service layer.
 */
public class EmployerRegistrationDto {

    /**
     * Full name of the employer registering on the platform.
     */
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;

    /**
     * Email address used for login and communication.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    /**
     * Password chosen by the employer.
     * Must contain uppercase, lowercase, digit and special character.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$", message = "Password must contain uppercase, lowercase, digit and special character")
    private String password;

    /**
     * Confirm password field used to verify password entry.
     */
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    /**
     * Name of the company associated with the employer.
     */
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 200, message = "Company name must be between 2 and 200 characters")
    private String companyName;

    /**
     * Industry sector in which the company operates.
     */
    @NotBlank(message = "Industry is required")
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    /**
     * Company size categorized by number of employees.
     */
    @NotBlank(message = "Company size is required")
    @Pattern(regexp = "^(1-10|11-50|51-200|201-500|500\\+)$", message = "Company size must be one of: 1-10, 11-50, 51-200, 201-500, 500+")
    private String companySize;

    /**
     * Official website URL of the company.
     * This field is optional.
     */
    @Pattern(regexp = "^(https?://)?([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}(/.*)?$", message = "Invalid website URL (e.g., example.com or https://example.com)")
    private String companyWebsite;

    /**
     * Description about the company, its mission or services.
     * This field is optional.
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String companyDescription;

    /**
     * Location of the company's headquarters.
     */
    @NotBlank(message = "Headquarters location is required")
    @Size(max = 150, message = "Headquarters must not exceed 150 characters")
    private String headquarters;

    /**
     * Security question used for account recovery.
     */
    @NotBlank(message = "Security question is required")
    private String securityQuestion;

    /**
     * Answer to the security question used for password recovery.
     */
    @NotBlank(message = "Security answer is required")
    @Size(min = 2, max = 100, message = "Security answer must be between 2 and 100 characters")
    private String securityAnswer;

    /**
     * Default constructor.
     */
    public EmployerRegistrationDto() {
    }

    // Getters and Setters

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getCompanySize() {
        return companySize;
    }

    public void setCompanySize(String companySize) {
        this.companySize = companySize;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getHeadquarters() {
        return headquarters;
    }

    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }
}