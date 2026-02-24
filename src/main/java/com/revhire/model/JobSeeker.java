package com.revhire.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "job_seekers")
@PrimaryKeyJoinColumn(name = "user_id")
public class JobSeeker extends User {
    
    private String currentEmploymentStatus;
    private String currentCompany;
    private String designation;
    private Integer totalExperienceYears;
    private String resumePath;
    private String resumeText;
    private String profileSummary;
    private String linkedInProfile;
    private String portfolioUrl;
    private String objective;
    private String degree;
    private String year;
    
    @ElementCollection
    @CollectionTable(name = "jobseeker_skills", joinColumns = @JoinColumn(name = "jobseeker_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC")
    private List<Education> education = new ArrayList<>();

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC")
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "jobSeeker")
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "jobSeeker")
    private List<SavedJob> savedJobs = new ArrayList<>();
    
    // Constructors
    public JobSeeker() {
        this.setRole(Role.JOBSEEKER);
    }
    
    // Getters and Setters for JobSeeker fields
    public String getCurrentEmploymentStatus() {
        return currentEmploymentStatus;
    }
    
    public void setCurrentEmploymentStatus(String currentEmploymentStatus) {
        this.currentEmploymentStatus = currentEmploymentStatus;
    }
    
    public String getCurrentCompany() {
        return currentCompany;
    }
    
    public void setCurrentCompany(String currentCompany) {
        this.currentCompany = currentCompany;
    }
    
    public String getDesignation() {
        return designation;
    }
    
    public void setDesignation(String designation) {
        this.designation = designation;
    }
    
    public Integer getTotalExperienceYears() {
        return totalExperienceYears;
    }
    
    public void setTotalExperienceYears(Integer totalExperienceYears) {
        this.totalExperienceYears = totalExperienceYears;
    }
    
    public String getResumePath() {
        return resumePath;
    }
    
    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }
    
    public String getResumeText() {
        return resumeText;
    }
    
    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }
    
    public String getProfileSummary() {
        return profileSummary;
    }
    
    public void setProfileSummary(String profileSummary) {
        this.profileSummary = profileSummary;
    }
    
    public String getLinkedInProfile() {
        return linkedInProfile;
    }
    
    public void setLinkedInProfile(String linkedInProfile) {
        this.linkedInProfile = linkedInProfile;
    }
    
    public String getPortfolioUrl() {
        return portfolioUrl;
    }
    
    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }
    
    public Set<String> getSkills() {
        return skills;
    }
    
    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }
    
    public List<Education> getEducation() {
        return education;
    }
    
    public void setEducation(List<Education> education) {
        this.education = education;
    }
    
    public List<Experience> getExperiences() {
        return experiences;
    }
    
    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
    }
    
    public List<Certification> getCertifications() {
        return certifications;
    }
    
    public void setCertifications(List<Certification> certifications) {
        this.certifications = certifications;
    }
    
    public List<Project> getProjects() {
        return projects;
    }
    
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
    
    public List<Application> getApplications() {
        return applications;
    }
    
    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }
    
    public List<SavedJob> getSavedJobs() {
        return savedJobs;
    }
    
    public void setSavedJobs(List<SavedJob> savedJobs) {
        this.savedJobs = savedJobs;
    }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    // Helper method
    public Double calculateProfileCompletion() {
        double completion = 0.0;
        if (profileSummary != null && !profileSummary.isEmpty()) completion += 10;
        if (resumePath != null) completion += 15;
        if (skills != null && !skills.isEmpty()) completion += 15;
        if (education != null && !education.isEmpty()) completion += 15;
        if (experiences != null && !experiences.isEmpty()) completion += 15;
        if (projects != null && !projects.isEmpty()) completion += 10;
        if (certifications != null && !certifications.isEmpty()) completion += 10;
        if (linkedInProfile != null && !linkedInProfile.isEmpty()) completion += 5;
        if (portfolioUrl != null && !portfolioUrl.isEmpty()) completion += 5;
        return completion;
    }
}