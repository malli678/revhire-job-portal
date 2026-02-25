package com.revhire.repository;

import com.revhire.model.Application;
import com.revhire.model.Job;
import com.revhire.model.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Prevent duplicate application
    Optional<Application> findByJobAndJobSeeker(Job job, JobSeeker jobSeeker);

    // Get applications by job seeker entity
    List<Application> findByJobSeeker(JobSeeker jobSeeker);

    // Get applications by job entity
    List<Application> findByJob(Job job);

    // ID-based queries using correct fields
    Optional<Application> findByJob_JobIdAndJobSeeker_UserId(Long jobId, Long jobSeekerId);
    List<Application> findByJobSeeker_UserId(Long jobSeekerId);
    List<Application> findByJob_JobId(Long jobId);
}