package com.revhire.service;

import com.revhire.model.Job;
import com.revhire.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // JOB DETAILS PAGE
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    // ADVANCED SEARCH
    public List<Job> searchJobsAdvanced(
            String title,
            String location,
            String experience,
            String jobType,
            String companyName,
            Double salaryMin,
            Double salaryMax,
            LocalDate postedDate
    ) {

        LocalDateTime date =
                postedDate != null ? postedDate.atStartOfDay() : null;

        return jobRepository.searchJobsAdvanced(
                title,
                location,
                experience,
                jobType,
                companyName,
                salaryMin,
                salaryMax,
                date
        );
    }
}