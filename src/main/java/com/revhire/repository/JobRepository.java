package com.revhire.repository;

import com.revhire.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    // BASIC SEARCH
    List<Job> findByTitleContainingIgnoreCase(String title);

    // ADVANCED SEARCH
    @Query("""
        SELECT j FROM Job j
        WHERE (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
        AND (:experience IS NULL OR LOWER(j.experienceRequired) LIKE LOWER(CONCAT('%', :experience, '%')))
        AND (:jobType IS NULL OR LOWER(j.jobType) = LOWER(:jobType))
        AND (:companyName IS NULL OR LOWER(j.employer.companyName) LIKE LOWER(CONCAT('%', :companyName, '%')))
        AND (:salaryMin IS NULL OR j.salaryMin >= :salaryMin)
        AND (:salaryMax IS NULL OR j.salaryMax <= :salaryMax)
        AND (:postedDate IS NULL OR j.postedDate >= :postedDate)
    """)
    List<Job> searchJobsAdvanced(
            @Param("title") String title,
            @Param("location") String location,
            @Param("experience") String experience,
            @Param("jobType") String jobType,
            @Param("companyName") String companyName,
            @Param("salaryMin") Double salaryMin,
            @Param("salaryMax") Double salaryMax,
            @Param("postedDate") LocalDateTime postedDate
    );
}