package com.revhire.dto;

import java.time.LocalDateTime;

public class JobDto {

    private String title;
    private String description;
    private String location;
    private String jobType;
    private String experienceRequired;
    private Double salaryMin;
    private Double salaryMax;
    private LocalDateTime deadline;
    private Integer numberOfOpenings;  // ✅ ADD THIS FIELD

    // Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(String experienceRequired) { this.experienceRequired = experienceRequired; }

    public Double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Double salaryMin) { this.salaryMin = salaryMin; }

    public Double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Double salaryMax) { this.salaryMax = salaryMax; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    
    // ✅ ADD GETTER/SETTER
    public Integer getNumberOfOpenings() { return numberOfOpenings; }
    public void setNumberOfOpenings(Integer numberOfOpenings) { this.numberOfOpenings = numberOfOpenings; }
}