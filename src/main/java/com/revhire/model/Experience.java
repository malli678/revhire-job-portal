package com.revhire.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "experience")
public class Experience {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exp_seq")
    @SequenceGenerator(name = "exp_seq", sequenceName = "EXPERIENCE_SEQ", allocationSize = 1)
    private Long experienceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobseeker_id", nullable = false)
    private JobSeeker jobSeeker;
    
    private String companyName;
    private String designation;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrentJob;
    private String description;
    
    // Getters and Setters
    public Long getExperienceId() { return experienceId; }
    public void setExperienceId(Long experienceId) { this.experienceId = experienceId; }
    
    public JobSeeker getJobSeeker() { return jobSeeker; }
    public void setJobSeeker(JobSeeker jobSeeker) { this.jobSeeker = jobSeeker; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public boolean isCurrentJob() { return isCurrentJob; }
    public void setCurrentJob(boolean currentJob) { isCurrentJob = currentJob; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}