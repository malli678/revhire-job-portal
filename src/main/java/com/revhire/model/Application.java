package com.revhire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Application entity represents a job application submitted by a job seeker.
 *
 * It stores details such as:
 * - The job applied for
 * - The job seeker who applied
 * - Application status
 * - Resume and cover letter information
 * - Notes added by employers
 * - Application timestamps
 *
 * Each job seeker can apply only once to a specific job,
 * enforced by a unique constraint on JOB_ID and JOBSEEKER_ID.
 */
@Entity
@Table(name = "APPLICATIONS", uniqueConstraints = @UniqueConstraint(columnNames = { "JOB_ID", "JOBSEEKER_ID" }))
public class Application {

    /**
     * Automatically sets the applied date and last updated date
     * when the application is first created.
     */
    @PrePersist
    protected void onCreate() {
        if (appliedDate == null) {
            appliedDate = LocalDateTime.now();
        }
        lastUpdatedDate = LocalDateTime.now();
    }

    /**
     * Updates the last updated timestamp whenever the entity is modified.
     */
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedDate = LocalDateTime.now();
    }

    /**
     * Enum representing different statuses of a job application.
     */
    public enum ApplicationStatus {
        APPLIED,
        UNDER_REVIEW,
        SHORTLISTED,
        REJECTED,
        WITHDRAWN
    }

    /**
     * Unique identifier for the application.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_seq_gen")
    @SequenceGenerator(name = "application_seq_gen", sequenceName = "APPLICATION_SEQ", allocationSize = 1)
    @Column(name = "APPLICATION_ID")
    private Long id;

    /**
     * Job associated with this application.
     */
    @ManyToOne
    @JoinColumn(name = "JOB_ID", nullable = false)
    private Job job;

    /**
     * Job seeker who submitted the application.
     */
    @ManyToOne
    @JoinColumn(name = "JOBSEEKER_ID", nullable = false)
    private JobSeeker jobSeeker;

    /**
     * Current status of the application.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private ApplicationStatus status;

    /**
     * Date and time when the application was submitted.
     */
    @Column(name = "APPLIED_DATE")
    private LocalDateTime appliedDate;

    /**
     * Notes added by the employer regarding the application.
     */
    @Column(name = "NOTES", length = 1000)
    private String notes;

    /**
     * Cover letter submitted by the job seeker.
     */
    @Column(name = "COVER_LETTER")
    private String coverLetter;

    /**
     * File path of the uploaded resume.
     */
    @Column(name = "RESUME_PATH")
    private String resumePath;

    /**
     * Timestamp showing when the application was last updated.
     */
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