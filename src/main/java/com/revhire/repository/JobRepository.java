package com.revhire.repository;

import com.revhire.model.Job;
import com.revhire.model.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // ===================================
    // SEARCH BY TITLE / ROLE
    // ===================================
    List<Job> findByTitleContainingIgnoreCase(String title);

    // ===================================
    // SEARCH BY LOCATION
    // ===================================
    List<Job> findByLocationContainingIgnoreCase(String location);

    // ===================================
    // SEARCH BY EXPERIENCE
    // ===================================
    List<Job> findByExperienceRequiredContainingIgnoreCase(String experience);

    // ===================================
    // SEARCH BY JOB TYPE
    // ===================================
    List<Job> findByJobTypeIgnoreCase(String jobType);

    // ===================================
    // SEARCH BY SALARY
    // ===================================
    List<Job> findBySalaryMinGreaterThanEqual(Double salary);

    // ===================================
    // SEARCH BY STATUS
    // ===================================
    List<Job> findByStatus(String status);

    // ===================================
    // GET JOBS BY EMPLOYER
    // ===================================
    List<Job> findByEmployer(Employer employer);

    // ===================================
    // DASHBOARD STATISTICS
    // ===================================
    long countByEmployerUserId(Long userId);
    long countByEmployerUserIdAndStatus(Long userId, String status);

    // ===================================
    // ✅ FIX: Add these new methods
    // ===================================
    
    // Find top 10 most recent jobs (for recommendations when user has no skills)
    List<Job> findTop10ByOrderByPostedDateDesc();
    
    // Alternative with limit parameter
    List<Job> findTopByOrderByPostedDateDesc(org.springframework.data.domain.Pageable pageable);
    
    // Filter by company name
    List<Job> findByEmployer_CompanyNameContainingIgnoreCase(String companyName);

    // Filter by date posted (last N days)
    @Query("SELECT j FROM Job j WHERE j.postedDate >= :date")
    List<Job> findByPostedDateAfter(@Param("date") LocalDateTime date);

    // Filter by experience range
    @Query("SELECT j FROM Job j WHERE " +
           "CAST(REGEXP_SUBSTR(j.experienceRequired, '\\d+') AS integer) BETWEEN :minExp AND :maxExp")
    List<Job> findByExperienceRange(@Param("minExp") int minExp, @Param("maxExp") int maxExp);

    // Combined advanced search
    @Query("SELECT j FROM Job j WHERE " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:company IS NULL OR LOWER(j.employer.companyName) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
           "(:jobType IS NULL OR j.jobType = :jobType) AND " +
           "(:minSalary IS NULL OR j.salaryMin >= :minSalary) AND " +
           "(:maxSalary IS NULL OR j.salaryMax <= :maxSalary)")
    List<Job> advancedSearch(
        @Param("title") String title,
        @Param("location") String location,
        @Param("company") String company,
        @Param("jobType") String jobType,
        @Param("minSalary") Double minSalary,
        @Param("maxSalary") Double maxSalary
    );
}