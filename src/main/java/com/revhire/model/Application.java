package com.revhire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "APPLICATIONS", uniqueConstraints = @UniqueConstraint(columnNames = { "JOB_ID", "JOBSEEKER_ID" }))
public class Application {

    @PrePersist
    protected void onCreate() {
        if (appliedDate == null) {
            appliedDate = LocalDateTime.now();
        }
        lastUpdatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedDate = LocalDateTime.now();
    }

    public enum ApplicationStatus {
        APPLIED,
        UNDER_REVIEW,
        SHORTLISTED,
        REJECTED,
        WITHDRAWN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_seq_gen")
    @SequenceGenerator(name = "application_seq_gen", sequenceName = "APPLICATION_SEQ", allocationSize = 1)
    @Column(name = "APPLICATION_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "JOB_ID", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "JOBSEEKER_ID", nullable = false)
    private JobSeeker jobSeeker;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private ApplicationStatus status;

    @Column(name = "APPLIED_DATE")
    private LocalDateTime appliedDate;

    @Column(name = "NOTES", length = 1000)
    private String notes;

    @Column(name = "COVER_LETTER")
    private String coverLetter;

    @Column(name = "RESUME_PATH")
    private String resumePath;

    @Column(name = "LAST_UPDATED_DATE")
    private LocalDateTime lastUpdatedDate;

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}