package com.revhire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_seq")
    @SequenceGenerator(name = "app_seq", sequenceName = "APPLICATION_SEQ", allocationSize = 1)
    private Long applicationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobseeker_id", nullable = false)
    private JobSeeker jobSeeker;
    
    private String status;
    private LocalDateTime appliedDate;
    private LocalDateTime lastUpdatedDate;
    private String coverLetter;
    private String resumePath;
    private String notes;
    
    public Application() {
        this.status = "APPLIED";
        this.appliedDate = LocalDateTime.now();
        this.lastUpdatedDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    
    public JobSeeker getJobSeeker() { return jobSeeker; }
    public void setJobSeeker(JobSeeker jobSeeker) { this.jobSeeker = jobSeeker; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDateTime appliedDate) { this.appliedDate = appliedDate; }
    
    public LocalDateTime getLastUpdatedDate() { return lastUpdatedDate; }
    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
    
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    
    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}