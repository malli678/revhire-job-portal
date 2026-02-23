package com.revhire.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import com.revhire.model.User.Role;

@Entity
@Table(name = "employers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Employer extends User {

    private String companyName;
    private String industry;
    private String companySize;
    private String companyWebsite;
    private String companyDescription;
    private String headquarters;

    // Keep jobs list for Job module
    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL)
    private List<Job> jobs = new ArrayList<>();

    // Constructor
    public Employer() {
        this.setRole(Role.EMPLOYER);
    }

    // =========================
    // Getters and Setters
    // =========================
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }

    public String getCompanyWebsite() { return companyWebsite; }
    public void setCompanyWebsite(String companyWebsite) { this.companyWebsite = companyWebsite; }

    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }

    public String getHeadquarters() { return headquarters; }
    public void setHeadquarters(String headquarters) { this.headquarters = headquarters; }

    public List<Job> getJobs() { return jobs; }
    public void setJobs(List<Job> jobs) { this.jobs = jobs; }
}