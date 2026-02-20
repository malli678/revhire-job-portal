package com.revhire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_jobs")
public class SavedJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "saved_job_seq")
    @SequenceGenerator(name = "saved_job_seq", sequenceName = "SAVED_JOB_SEQ", allocationSize = 1)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobseeker_id", nullable = false)
    private JobSeeker jobSeeker;
    
    private LocalDateTime savedDate;
    
    public SavedJob() {
        this.savedDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    
    public JobSeeker getJobSeeker() { return jobSeeker; }
    public void setJobSeeker(JobSeeker jobSeeker) { this.jobSeeker = jobSeeker; }
    
    public LocalDateTime getSavedDate() { return savedDate; }
    public void setSavedDate(LocalDateTime savedDate) { this.savedDate = savedDate; }
}