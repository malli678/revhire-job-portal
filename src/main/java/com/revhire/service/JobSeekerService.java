package com.revhire.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.revhire.exception.ResourceNotFoundException;
import com.revhire.exception.FileStorageException; // reuse for business validation if needed
import com.revhire.model.*;
import com.revhire.model.Application.ApplicationStatus;
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

    // =========================
    // SAVE JOB
    // =========================
    public ResponseEntity<?> saveJob(Long jobSeekerId, Long jobId) {

        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Check if already saved
        savedJobRepository.findByJobSeekerAndJob(jobSeeker, job)
                .ifPresent(s -> { throw new FileStorageException("Job already saved"); });

        SavedJob savedJob = new SavedJob();
        savedJob.setJobSeeker(jobSeeker);
        savedJob.setJob(job);

        savedJobRepository.save(savedJob);

        return ResponseEntity.ok("Job saved successfully");
    }

    // =========================
    // APPLY JOB
    // =========================
    public ResponseEntity<String> applyJob(Long userId, Long jobId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        JobSeeker jobSeeker = jobSeekerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("JobSeeker not found"));

        if (applicationRepository.findByJobAndJobSeeker(job, jobSeeker).isPresent()) {
            return ResponseEntity.badRequest().body("Already applied for this job");
        }

        Application app = new Application();
        app.setJob(job);
        app.setJobSeeker(jobSeeker);
        app.setStatus(Application.ApplicationStatus.APPLIED);
        app.setAppliedDate(LocalDateTime.now());

        applicationRepository.save(app);

        return ResponseEntity.ok("Application Submitted Successfully!");
    }
    // =========================
    // GET ALL JOBS
    // =========================
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // =========================
    // GET SAVED JOBS
    // =========================
    public List<SavedJob> getSavedJobsList(Long jobSeekerId) {

        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        return savedJobRepository.findByJobSeeker(jobSeeker);
    }

    // =========================
    // GET APPLICATIONS
    // =========================
    public List<Application> getApplicationsList(Long jobSeekerId) { 
        return applicationRepository.findByJobSeeker_UserId(jobSeekerId);
    }
    // =========================
    // GET JOB BY ID
    // =========================
    public Job getJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    // =========================
    // WITHDRAW APPLICATION
    // =========================
    public void withdrawApplication(Long applicationId, String notes) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // ✅ SAFETY CHECK ⭐⭐⭐
        if (application.getStatus() != Application.ApplicationStatus.APPLIED) {
            throw new RuntimeException("Only APPLIED applications can be withdrawn");
        }

        application.setStatus(Application.ApplicationStatus.WITHDRAWN);
        application.setNotes(notes);

        applicationRepository.save(application);
    }
}