package com.revhire.dto;

import java.time.LocalDateTime;

/**
 * ApplicationDto is a Data Transfer Object used to transfer
 * job application data between different layers of the application.
 *
 * This DTO contains essential information about a job application
 * such as job ID, job seeker ID, application status, applied date,
 * and any notes added by the employer.
 */
public class ApplicationDto {

    /**
     * Unique identifier of the application.
     */
    private Long id;

    /**
     * ID of the job for which the application was submitted.
     */
    private Long jobId;

    /**
     * ID of the job seeker who submitted the application.
     */
    private Long jobSeekerId;

    /**
     * Current status of the application.
     * Example values: APPLIED, SHORTLISTED, REJECTED, WITHDRAWN.
     */
    private String status;

    /**
     * Date and time when the application was submitted.
     */
    private LocalDateTime appliedDate;

    /**
     * Notes added by the employer regarding the application.
     */
    private String notes;

    /**
     * Default constructor.
     */
    public ApplicationDto() {}

    /**
     * Parameterized constructor used to create an ApplicationDto object.
     *
     * @param id application ID
     * @param jobId job ID
     * @param jobSeekerId job seeker ID
     * @param status application status
     * @param appliedDate date and time when application was submitted
     * @param notes employer notes related to the application
     */
    public ApplicationDto(Long id, Long jobId, Long jobSeekerId,
                          String status, LocalDateTime appliedDate, String notes) {
        this.id = id;
        this.jobId = jobId;
        this.jobSeekerId = jobSeekerId;
        this.status = status;
        this.appliedDate = appliedDate;
        this.notes = notes;
    }

    /**
     * Returns the application ID.
     */
    public Long getId() { return id; }

    /**
     * Returns the job ID.
     */
    public Long getJobId() { return jobId; }

    /**
     * Returns the job seeker ID.
     */
    public Long getJobSeekerId() { return jobSeekerId; }

    /**
     * Returns the application status.
     */
    public String getStatus() { return status; }

    /**
     * Returns the date when the application was submitted.
     */
    public LocalDateTime getAppliedDate() { return appliedDate; }

    /**
     * Returns the notes added to the application.
     */
    public String getNotes() { return notes; }
}