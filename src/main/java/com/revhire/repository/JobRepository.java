package com.revhire.repository;

import com.revhire.model.Job;
import com.revhire.model.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
    // DASHBOARD STATISTICS ⭐⭐⭐
    // ===================================

    // ✅ TOTAL JOBS
    
    long countByEmployerUserId(Long userId);

    long countByEmployerUserIdAndStatus(Long userId, String status);
}