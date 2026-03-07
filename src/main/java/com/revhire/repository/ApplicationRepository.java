package com.revhire.repository;

import com.revhire.model.Application;
import com.revhire.model.Job;
import com.revhire.model.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ApplicationRepository is responsible for performing database operations
 * related to the Application entity.
 *
 * It extends JpaRepository which provides built-in CRUD operations such as:
 * - save()
 * - findById()
 * - findAll()
 * - delete()
 *
 * In addition to these, this repository defines custom query methods
 * to retrieve applications based on job, job seeker, employer, and filters.
 */
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Finds an application by job and job seeker.
     * Used to check if a job seeker already applied for a job.
     */
    Optional<Application> findByJobAndJobSeeker(Job job, JobSeeker jobSeeker);

    /**
     * Retrieves all applications submitted by a specific job seeker.
     */
    List<Application> findByJobSeeker(JobSeeker jobSeeker);

    /**
     * Retrieves all applications for a specific job.
     */
    List<Application> findByJob(Job job);

    /**
     * Finds an application using job ID and job seeker ID.
     */
    Optional<Application> findByJob_JobIdAndJobSeeker_UserId(Long jobId, Long jobSeekerId);

    /**
     * Retrieves applications submitted by a job seeker using their user ID.
     */
    List<Application> findByJobSeeker_UserId(Long jobSeekerId);

    /**
     * Retrieves all applications for a job using the job ID.
     */
    List<Application> findByJob_JobId(Long jobId);

    // ================= DASHBOARD =================

    /**
     * Retrieves applications for jobs posted by a specific employer.
     */
    List<Application> findByJob_Employer_UserId(Long userId);

    /**
     * Counts the total number of applications received by an employer.
     */
    long countByJob_Employer_UserId(Long userId);

    /**
     * Counts the number of applications for a specific job.
     */
    long countByJob_JobId(Long jobId);

    /**
     * Counts applications by employer and application status.
     * Useful for dashboard statistics.
     */
    long countByJob_Employer_UserIdAndStatus(
            Long userId,
            Application.ApplicationStatus status
    );

    // ================= FILTERS =================

    /**
     * Retrieves applications filtered by employer and application status.
     */
    List<Application> findByJob_Employer_UserIdAndStatus(
            Long employerId,
            Application.ApplicationStatus status
    );

    /**
     * Retrieves applications where the job seeker has a specific skill.
     */
    List<Application> findByJob_Employer_UserIdAndJobSeeker_SkillsContaining(
            Long employerId,
            String skill
    );

    /**
     * Retrieves applications where the job seeker has
     * experience greater than or equal to the specified value.
     */
    List<Application> findByJob_Employer_UserIdAndJobSeeker_TotalExperienceYearsGreaterThanEqual(
            Long employerId,
            Integer experience
    );

    /**
     * Retrieves applications where the job seeker has a specific degree.
     */
    List<Application> findByJob_Employer_UserIdAndJobSeeker_DegreeContainingIgnoreCase(
            Long employerId,
            String degree
    );

    /**
     * Retrieves applications submitted after a specific date.
     */
    List<Application> findByJob_Employer_UserIdAndAppliedDateAfter(
            Long employerId,
            LocalDateTime date
    );
}