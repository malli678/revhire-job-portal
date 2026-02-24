package com.revhire.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.revhire.exception.ResourceNotFoundException;
import com.revhire.exception.FileStorageException; // reuse for business validation if needed
import com.revhire.model.*;
import com.revhire.repository.*;
@Service
public class JobSeekerService {

    @Autowired
    private SavedJobRepository savedJobRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSeekerRepository jobSeekerRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    public ResponseEntity<?> saveJob(Long jobSeekerId, Long jobId) {

        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        savedJobRepository.findByJobSeekerAndJob(jobSeeker, job)
                .ifPresent(s -> { throw new FileStorageException("Job already saved"); });

        SavedJob savedJob = new SavedJob();
        savedJob.setJobSeeker(jobSeeker);
        savedJob.setJob(job);

        savedJobRepository.save(savedJob);

        return ResponseEntity.ok("Job saved successfully");
    }
    
    public ResponseEntity<String> applyJob(Long userId, Long jobId) {

        JobSeeker jobSeeker = jobSeekerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("JobSeeker not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // ✅ CHECK DUPLICATE FIRST
        boolean alreadyApplied =
                applicationRepository.findByJobAndJobSeeker(job, jobSeeker).isPresent();

        if (alreadyApplied) {
            return ResponseEntity.badRequest()
                    .body("Already applied for this job");
        }

        Application application = new Application();
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setAppliedDate(LocalDateTime.now());
        application.setStatus(Application.ApplicationStatus.APPLIED);

        applicationRepository.save(application);

        return ResponseEntity.ok("Applied Successfully");
    }
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<SavedJob> getSavedJobsList(Long id) {
        JobSeeker js = jobSeekerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        return savedJobRepository.findByJobSeeker(js);
    }

    public List<Application> getApplicationsList(Long id) {
        JobSeeker js = jobSeekerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        return applicationRepository.findByJobSeeker(js);
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }
    public void withdrawApplication(Long applicationId, String notes) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(Application.ApplicationStatus.WITHDRAWN);
        application.setNotes(notes);

        applicationRepository.save(application);
     
    }
}