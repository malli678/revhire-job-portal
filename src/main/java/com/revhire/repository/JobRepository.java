package com.revhire.repository;

import com.revhire.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    // Search by Role
    List<Job> findByTitleContainingIgnoreCase(String title);

    // Search by Location
    List<Job> findByLocationContainingIgnoreCase(String location);

    // Search by Experience
    List<Job> findByExperienceRequiredContainingIgnoreCase(String experience);

    // Search by Job Type
    List<Job> findByJobTypeIgnoreCase(String jobType);

    // Search by Salary
    List<Job> findBySalaryMinGreaterThanEqual(Double salary);
}