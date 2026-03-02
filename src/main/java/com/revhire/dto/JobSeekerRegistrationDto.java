package com.revhire.dto;

import jakarta.validation.constraints.*;

public class JobSeekerRegistrationDto {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Full name can contain only alphabets and spaces")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @NotBlank(message = "Current employment status is required")
    @Pattern(
            regexp = "^(EMPLOYED|UNEMPLOYED|STUDENT|FRESHER|FREELANCER)$",
            message = "Employment status must be EMPLOYED, UNEMPLOYED, STUDENT, FRESHER, or FREELANCER"
    )
    private String currentEmploymentStatus;

    @Size(max = 150, message = "Current company must not exceed 150 characters")
    private String currentCompany;

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience cannot exceed 50 years")
    private Integer totalExperienceYears;  // Removed @NotNull

    // Default Constructor
    public JobSeekerRegistrationDto() {
    }

    // Getters and Setters (same as yours)
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCurrentEmploymentStatus() { return currentEmploymentStatus; }
    public void setCurrentEmploymentStatus(String currentEmploymentStatus) { this.currentEmploymentStatus = currentEmploymentStatus; }

    public String getCurrentCompany() { return currentCompany; }
    public void setCurrentCompany(String currentCompany) { this.currentCompany = currentCompany; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getTotalExperienceYears() { return totalExperienceYears; }
    public void setTotalExperienceYears(Integer totalExperienceYears) { this.totalExperienceYears = totalExperienceYears; }
}