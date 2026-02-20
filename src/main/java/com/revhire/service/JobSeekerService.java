package com.revhire.service;

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
    private UserRepository userRepository;

    // ✅ Add to Favourite
    public ResponseEntity<?> saveJob(Long userId, Long jobId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // ✅ Prevent duplicate save
        savedJobRepository.findByUserAndJob(user, job)
                .ifPresent(s -> {
                    throw new FileStorageException("Job already saved");
                });

        SavedJob savedJob = new SavedJob();
        savedJob.setUser(user);
        savedJob.setJob(job);

        savedJobRepository.save(savedJob);

        return ResponseEntity.ok("Job saved successfully");
    }

    // ✅ Remove Favourite
    public ResponseEntity<?> removeSavedJob(Long userId, Long jobId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        SavedJob savedJob = savedJobRepository.findByUserAndJob(user, job)
                .orElseThrow(() -> new ResourceNotFoundException("Saved job not found"));

        savedJobRepository.delete(savedJob);

        return ResponseEntity.ok("Saved job removed successfully");
    }

    // ✅ View Saved Jobs
    public ResponseEntity<?> getSavedJobs(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<SavedJob> savedJobs = savedJobRepository.findByUser(user);

        return ResponseEntity.ok(savedJobs);
    }
}