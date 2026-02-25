package com.revhire.service;

import com.revhire.dto.CompanyProfileUpdateDto;
import com.revhire.model.Application;
import com.revhire.model.Employer;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.EmployerRepository;
import com.revhire.repository.JobRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployerService {

    private final EmployerRepository employerRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public EmployerService(EmployerRepository employerRepository,
                           JobRepository jobRepository,
                           ApplicationRepository applicationRepository) {
        this.employerRepository = employerRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    // =====================================
    // EMPLOYER FETCH
    // =====================================

    public Employer getEmployerByEmail(String email) {
        return employerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
    }

    public Employer getEmployerById(Long id) {
        return employerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
    }

    public Employer saveEmployer(Employer employer) {
        return employerRepository.save(employer);
    }

    // =====================================
    // APPLICATION FETCH ⭐⭐⭐
    // =====================================

    public List<Application> getApplicationsForEmployer(Employer employer) {
        return applicationRepository.findByJob_Employer_UserId(
                employer.getUserId()
        );
    }

    // =====================================
    // DASHBOARD STATISTICS ⭐⭐⭐
    // =====================================

    // ✅ TOTAL JOBS
    public long countTotalJobs(Employer employer) {
        return jobRepository.countByEmployerUserId(
                employer.getUserId()
        );
    }

    // ✅ ACTIVE JOBS
    public long countActiveJobs(Employer employer) {
        return jobRepository.countByEmployerUserIdAndStatus(
                employer.getUserId(),
                "ACTIVE"
        );
    }

    // ✅ TOTAL APPLICATIONS
    public long countTotalApplications(Employer employer) {
        return applicationRepository.countByJob_Employer_UserId(
                employer.getUserId()
        );
    }

    // ✅ PENDING REVIEWS (APPLIED only)
    public long countPendingReviews(Employer employer) {
        return applicationRepository.countByJob_Employer_UserIdAndStatus(
                employer.getUserId(),
                Application.ApplicationStatus.APPLIED
        );
    }

    // ✅ SHORTLISTED COUNT
    public long countShortlisted(Employer employer) {
        return applicationRepository.countByJob_Employer_UserIdAndStatus(
                employer.getUserId(),
                Application.ApplicationStatus.SHORTLISTED
        );
    }

    // ✅ REJECTED COUNT ⭐⭐⭐ (NEW)
    public long countRejectedApplications(Employer employer) {
        return applicationRepository.countByJob_Employer_UserIdAndStatus(
                employer.getUserId(),
                Application.ApplicationStatus.REJECTED
        );
    }

    // ✅ GENERIC STATUS COUNT
    public long countByStatus(Employer employer,
                              Application.ApplicationStatus status) {

        return applicationRepository.countByJob_Employer_UserIdAndStatus(
                employer.getUserId(),
                status
        );
    }
    public void updateCompanyProfile(Long employerId,
            CompanyProfileUpdateDto dto) {

Employer employer = employerRepository.findById(employerId)
.orElseThrow(() -> new RuntimeException("Employer not found"));

// ✅ Update only editable fields
employer.setCompanyName(dto.getCompanyName());
employer.setIndustry(dto.getIndustry());
employer.setCompanySize(dto.getCompanySize());
employer.setCompanyWebsite(dto.getCompanyWebsite());
employer.setCompanyDescription(dto.getCompanyDescription());
employer.setHeadquarters(dto.getHeadquarters());

employerRepository.save(employer);
}
    //filters
 // =====================================
 // FILTER APPLICATIONS ⭐⭐⭐⭐⭐
 // =====================================
 public List<Application> filterApplications(
         Employer employer,
         String skill,
         Integer experience,
         String education,
         String status,
         LocalDateTime date
 ) {

     Long employerId = employer.getUserId();

     // ✅ FILTER BY STATUS
     if (status != null && !status.isBlank()) {

         return applicationRepository
                 .findByJob_Employer_UserIdAndStatus(
                         employerId,
                         Application.ApplicationStatus.valueOf(status)
                 );
     }

     // ✅ FILTER BY SKILLS
     if (skill != null && !skill.isBlank()) {

         return applicationRepository
                 .findByJob_Employer_UserIdAndJobSeeker_SkillsContaining(
                         employerId,
                         skill
                 );
     }

     // ✅ FILTER BY EXPERIENCE ⭐ FIXED SAFE CONVERSION
     if (experience != null) {

         return applicationRepository
                 .findByJob_Employer_UserIdAndJobSeeker_TotalExperienceYearsGreaterThanEqual(
                         employerId,
                         experience
                 );
     }

     // ✅ FILTER BY EDUCATION
     if (education != null && !education.isBlank()) {

         return applicationRepository
                 .findByJob_Employer_UserIdAndJobSeeker_DegreeContainingIgnoreCase(
                         employerId,
                         education
                 );
     }

     // ✅ FILTER BY DATE
     if (date != null) {

         return applicationRepository
                 .findByJob_Employer_UserIdAndAppliedDateAfter(
                         employerId,
                         date
                 );
     }

     // ✅ DEFAULT → ALL APPLICATIONS
     return applicationRepository
             .findByJob_Employer_UserId(employerId);
 }
}