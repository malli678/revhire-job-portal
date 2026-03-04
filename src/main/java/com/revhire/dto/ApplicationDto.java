package com.revhire.dto;

import java.time.LocalDateTime;

public class ApplicationDto {

    private Long id;
    private Long jobId;
    private Long jobSeekerId;
    private String status;
    private LocalDateTime appliedDate;
    private String notes;

    public ApplicationDto() {}

    public ApplicationDto(Long id, Long jobId, Long jobSeekerId,
                          String status, LocalDateTime appliedDate, String notes) {
        this.id = id;
        this.jobId = jobId;
        this.jobSeekerId = jobSeekerId;
        this.status = status;
        this.appliedDate = appliedDate;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public Long getJobSeekerId() { return jobSeekerId; }
    public String getStatus() { return status; }
    public LocalDateTime getAppliedDate() { return appliedDate; }
    public String getNotes() { return notes; }
}