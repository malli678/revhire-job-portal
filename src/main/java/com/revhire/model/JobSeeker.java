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
    private String resumeFile;

    public String getResumeFile() {
        return resumeFile;
    }

    public void setResumeFile(String resumeFile) {
        this.resumeFile = resumeFile;
    }

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

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL)
    private List<Skill> skillEntities;

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL)
    private List<Education> educationEntities;

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL)
    private List<Certification> certificationEntities;

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

    public List<Skill> getSkillEntities() {
        return skillEntities;
    }

    public void setSkillEntities(List<Skill> skillEntities) {
        this.skillEntities = skillEntities;
    }

    public List<Education> getEducationEntities() {
        return educationEntities;
    }

    public void setEducationEntities(List<Education> educationEntities) {
        this.educationEntities = educationEntities;
    }

    public List<Certification> getCertificationEntities() {
        return certificationEntities;
    }

    public void setCertificationEntities(List<Certification> certificationEntities) {
        this.certificationEntities = certificationEntities;
    }

    public List<SavedJob> getSavedJobs() {
        return savedJobs;
    }

    public void setSavedJobs(List<SavedJob> savedJobs) {
        this.savedJobs = savedJobs;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Double calculateProfileCompletion() {
        int totalFields = 0;
        int filledFields = 0;

        // Personal details (3 fields)
        if (getPhoneNumber() != null && !getPhoneNumber().isEmpty())
            filledFields++;
        if (getLocation() != null && !getLocation().isEmpty())
            filledFields++;
        totalFields += 2;

        // Employment (4 fields) - Fresher is valid
        if (currentEmploymentStatus != null && !currentEmploymentStatus.isEmpty())
            filledFields++;
        if (currentCompany != null && !currentCompany.isEmpty())
            filledFields++;
        if (designation != null && !designation.isEmpty())
            filledFields++;
        if (totalExperienceYears != null)
            filledFields++; // 0 is valid for fresher
        totalFields += 4;

        // Skills (at least 3 skills count as complete)
        int skillCount = 0;
        if (skills != null)
            skillCount += skills.size();
        if (skillEntities != null)
            skillCount += skillEntities.size();
        filledFields += Math.min(skillCount, 3);
        totalFields += 3;

        // Education (at least 1)
        if ((education != null && !education.isEmpty()) ||
                (educationEntities != null && !educationEntities.isEmpty())) {
            filledFields++;
        }
        totalFields += 1;

        // Certifications (at least 1)
        if ((certifications != null && !certifications.isEmpty()) ||
                (certificationEntities != null && !certificationEntities.isEmpty())) {
            filledFields++;
        }
        totalFields += 1;

        // Experience (internship counts)
        if (experiences != null && !experiences.isEmpty()) {
            filledFields++;
        }
        totalFields += 1;

        // Resume (2 fields)
        if (resumeFile != null && !resumeFile.isEmpty())
            filledFields++;
        if (objective != null && !objective.isEmpty())
            filledFields++;
        totalFields += 2;

        // Calculate percentage
        double completion = (filledFields * 100.0) / totalFields;

        // Round to 1 decimal place
        return Math.round(completion * 10) / 10.0;
    }
}