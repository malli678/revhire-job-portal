package com.revhire.service;

import com.revhire.model.Job;
import com.revhire.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // Post Job
    public Job saveJob(Job job) {
        job.setPostedDate(LocalDateTime.now());
        return jobRepository.save(job);
    }

    // Get all jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // Job details
    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElse(null);
    }

    // Delete
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    // Search
    public List<Job> searchByLocation(String location) {
        return jobRepository.findByLocation(location);
    }

    public List<Job> searchByTitle(String title) {
        return jobRepository.findByTitleContaining(title);
    }

    // Status update
    public Job updateStatus(Long id, String status) {
        Job job = getJobById(id);
        if (job != null) {
            job.setStatus(status);
            return jobRepository.save(job);
        }
        return null;
    }
}