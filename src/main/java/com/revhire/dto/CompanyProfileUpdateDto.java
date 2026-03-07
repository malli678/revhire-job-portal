package com.revhire.dto;

/**
 * CompanyProfileUpdateDto is a Data Transfer Object used to update
 * employer company profile information.
 *
 * This DTO carries company-related details from the frontend
 * to the backend service layer when an employer updates
 * their company profile.
 */
public class CompanyProfileUpdateDto {

    /**
     * Name of the company.
     */
    private String companyName;

    /**
     * Industry sector the company belongs to.
     */
    private String industry;

    /**
     * Size of the company based on number of employees.
     */
    private String companySize;

    /**
     * Official website URL of the company.
     */
    private String companyWebsite;

    /**
     * Description about the company including its mission or services.
     */
    private String companyDescription;

    /**
     * Location of the company's headquarters.
     */
    private String headquarters;

    // Getters & Setters

    /**
     * Returns the company name.
     */
    public String getCompanyName() { return companyName; }

    /**
     * Sets the company name.
     */
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    /**
     * Returns the industry of the company.
     */
    public String getIndustry() { return industry; }

    /**
     * Sets the industry of the company.
     */
    public void setIndustry(String industry) { this.industry = industry; }

    /**
     * Returns the company size.
     */
    public String getCompanySize() { return companySize; }

    /**
     * Sets the company size.
     */
    public void setCompanySize(String companySize) { this.companySize = companySize; }

    /**
     * Returns the company website.
     */
    public String getCompanyWebsite() { return companyWebsite; }

    /**
     * Sets the company website.
     */
    public void setCompanyWebsite(String companyWebsite) { this.companyWebsite = companyWebsite; }

    /**
     * Returns the company description.
     */
    public String getCompanyDescription() { return companyDescription; }

    /**
     * Sets the company description.
     */
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }

    /**
     * Returns the company headquarters location.
     */
    public String getHeadquarters() { return headquarters; }

    /**
     * Sets the company headquarters location.
     */
    public void setHeadquarters(String headquarters) { this.headquarters = headquarters; }
}