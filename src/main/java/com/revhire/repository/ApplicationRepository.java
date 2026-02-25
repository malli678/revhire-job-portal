package com.revhire.repository;

import com.revhire.model.Application;
import com.revhire.model.Job;
import com.revhire.model.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByJobAndJobSeeker(Job job, JobSeeker jobSeeker);

    List<Application> findByJobSeeker(JobSeeker jobSeeker);

    List<Application> findByJob(Job job);

    Optional<Application> findByJob_JobIdAndJobSeeker_UserId(Long jobId, Long jobSeekerId);

    List<Application> findByJobSeeker_UserId(Long jobSeekerId);

    List<Application> findByJob_JobId(Long jobId);

    // DASHBOARD
    List<Application> findByJob_Employer_UserId(Long userId);

    long countByJob_Employer_UserId(Long userId);

    long countByJob_Employer_UserIdAndStatus(
            Long userId,
            Application.ApplicationStatus status
    );

    // FILTERS ⭐⭐⭐⭐⭐

    List<Application> findByJob_Employer_UserIdAndStatus(
            Long employerId,
            Application.ApplicationStatus status
    );

    List<Application> findByJob_Employer_UserIdAndJobSeeker_SkillsContaining(
            Long employerId,
            String skill
    );

    // ✅ FIXED TYPE ⭐⭐⭐
    List<Application> findByJob_Employer_UserIdAndJobSeeker_TotalExperienceYearsGreaterThanEqual(
            Long employerId,
            Integer experience
    );

    List<Application> findByJob_Employer_UserIdAndJobSeeker_DegreeContainingIgnoreCase(
            Long employerId,
            String degree
    );

    List<Application> findByJob_Employer_UserIdAndAppliedDateAfter(
            Long employerId,
            LocalDateTime date
    );
}