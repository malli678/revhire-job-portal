package com.revhire.service;

import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.repository.EmployerRepository;
import com.revhire.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmployerService {

    private final EmployerRepository employerRepository;
    private final JobRepository jobRepository;

    public EmployerService(EmployerRepository employerRepository,
                           JobRepository jobRepository) {
        this.employerRepository = employerRepository;
        this.jobRepository = jobRepository;
    }

    // =========================
    // GET EMPLOYER
    // =========================
    public Employer getEmployerByEmail(String email) {
        return employerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
    }

    // =========================
    // POST JOB
    // =========================
    public Job postJob(Job job, Employer employer) {

        if (job == null || employer == null) {
            throw new RuntimeException("Invalid Job or Employer");
        }

        job.setEmployer(employer);
        job.setStatus("ACTIVE");
        job.setCreatedAt(LocalDateTime.now());
        job.setPostedDate(LocalDateTime.now());

        return jobRepository.save(job);
    }

    // =========================
    // UPDATE JOB
    // =========================
    public Job updateJob(Long id, Job updated) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setTitle(updated.getTitle());
        job.setDescription(updated.getDescription());
        job.setLocation(updated.getLocation());
        job.setJobType(updated.getJobType());
        job.setExperienceRequired(updated.getExperienceRequired());
        job.setSalaryMin(updated.getSalaryMin());
        job.setSalaryMax(updated.getSalaryMax());
        job.setUpdatedAt(LocalDateTime.now());

        return jobRepository.save(job);
    }

    // =========================
    // DELETE
    // =========================
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    // =========================
    // CLOSE
    // =========================
    public Job closeJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("CLOSED");
        return jobRepository.save(job);
    }

    // =========================
    // REOPEN
    // =========================
    public Job reopenJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("ACTIVE");
        return jobRepository.save(job);
    }

    // =========================
    // FILLED
    // =========================
    public Job markFilled(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("FILLED");
        return jobRepository.save(job);
    }
}