package com.revhire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.revhire.model.Application;
import com.revhire.model.Job;
import com.revhire.model.JobSeeker;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Prevent duplicate application
    Optional<Application> findByJobAndJobSeeker(Job job, JobSeeker jobSeeker);

    // Get applications by job seeker
    List<Application> findByJobSeeker(JobSeeker jobSeeker);

    // Get applications by job
    List<Application> findByJob(Job job);
}