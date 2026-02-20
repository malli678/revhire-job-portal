package com.revhire.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "certifications")
public class Certification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cert_seq")
    @SequenceGenerator(name = "cert_seq", sequenceName = "CERTIFICATION_SEQ", allocationSize = 1)
    private Long certificationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobseeker_id", nullable = false)
    private JobSeeker jobSeeker;
    
    private String name;
    private String issuingOrganization;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private String credentialId;
    private String credentialUrl;
    
    // Getters and Setters
    public Long getCertificationId() { return certificationId; }
    public void setCertificationId(Long certificationId) { this.certificationId = certificationId; }
    
    public JobSeeker getJobSeeker() { return jobSeeker; }
    public void setJobSeeker(JobSeeker jobSeeker) { this.jobSeeker = jobSeeker; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getIssuingOrganization() { return issuingOrganization; }
    public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }
    
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    
    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
    
    public String getCredentialUrl() { return credentialUrl; }
    public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }
}