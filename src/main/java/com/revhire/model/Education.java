package com.revhire.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "education")
public class Education {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "edu_seq")
    @SequenceGenerator(name = "edu_seq", sequenceName = "EDUCATION_SEQ", allocationSize = 1)
    private Long educationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobseeker_id", nullable = false)
    private JobSeeker jobSeeker;
    
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String grade;
    private String description;
    
    // Getters and Setters
    public Long getEducationId() { return educationId; }
    public void setEducationId(Long educationId) { this.educationId = educationId; }
    
    public JobSeeker getJobSeeker() { return jobSeeker; }
    public void setJobSeeker(JobSeeker jobSeeker) { this.jobSeeker = jobSeeker; }
    
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
    
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    
    public String getFieldOfStudy() { return fieldOfStudy; }
    public void setFieldOfStudy(String fieldOfStudy) { this.fieldOfStudy = fieldOfStudy; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}