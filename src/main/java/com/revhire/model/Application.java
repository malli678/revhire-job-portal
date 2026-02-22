package com.revhire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications",
       uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "job_seeker_id"}))
public class Application {

    // ===== ENUM (Simple Version - 4 Status) =====
    public enum ApplicationStatus {
        APPLIED,
        SHORTLISTED,
        REJECTED,
        WITHDRAWN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many applications can belong to one job
    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Many applications can belong to one job seeker
    @ManyToOne
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private JobSeeker jobSeeker;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDateTime appliedDate;
    
    //Note
    @Column(length = 1000)
    private String notes;

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public JobSeeker getJobSeeker() {
        return jobSeeker;
    }

    public void setJobSeeker(JobSeeker jobSeeker) {
        this.jobSeeker = jobSeeker;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDateTime appliedDate) {
        this.appliedDate = appliedDate;
    }
    
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}