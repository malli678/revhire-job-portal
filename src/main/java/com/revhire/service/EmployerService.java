package com.revhire.service;

import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.repository.EmployerRepository;
import com.revhire.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployerService {

    private final EmployerRepository employerRepository;
    private final JobRepository jobRepository;

    public EmployerService(EmployerRepository employerRepository,
                           JobRepository jobRepository) {
        this.employerRepository = employerRepository;
        this.jobRepository = jobRepository;
    }

    // =====================================
    // EMPLOYER OPERATIONS
    // =====================================

    public Employer getEmployerByEmail(String email) {
        return employerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
    }

    public Employer saveEmployer(Employer employer) {
        return employerRepository.save(employer);
    }

    public List<Employer> getAllEmployers() {
        return employerRepository.findAll();
    }

    public Employer getEmployerById(Long id) {
        return employerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
    }

    // ⭐ VERY IMPORTANT (FOR MANAGE JOBS PAGE)
    public List<Job> getJobsByEmployer(Employer employer) {
        return jobRepository.findByEmployer(employer);
    }

    // =====================================
    // JOB OPERATIONS
    // =====================================

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

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    public Job closeJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("CLOSED");
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    public Job reopenJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("ACTIVE");
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    public Job markFilled(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("FILLED");
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }
}