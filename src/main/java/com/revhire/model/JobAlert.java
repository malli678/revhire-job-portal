package com.revhire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_alerts")
public class JobAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alert_seq")
    @SequenceGenerator(name = "alert_seq", sequenceName = "JOB_ALERT_SEQ", allocationSize = 1)
    private Long alertId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobseeker_id", nullable = false)
    private JobSeeker jobSeeker;
    
    private String alertName;
    private String keywords;
    private String location;
    private String jobType;
    private Integer minSalary;
    private String frequency; // DAILY, WEEKLY, INSTANT
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastSentAt;
    
    // Constructors
    public JobAlert() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.frequency = "DAILY";
    }
    
    // Getters and Setters
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    
    public JobSeeker getJobSeeker() { return jobSeeker; }
    public void setJobSeeker(JobSeeker jobSeeker) { this.jobSeeker = jobSeeker; }
    
    public String getAlertName() { return alertName; }
    public void setAlertName(String alertName) { this.alertName = alertName; }
    
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    
    public Integer getMinSalary() { return minSalary; }
    public void setMinSalary(Integer minSalary) { this.minSalary = minSalary; }
    
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(LocalDateTime lastSentAt) { this.lastSentAt = lastSentAt; }
}