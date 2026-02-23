package com.revhire.service;

import com.revhire.dto.JobDto;
import com.revhire.model.Employer;
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

    // =========================
    // SAVE JOB (Thymeleaf)
    // =========================
    public Job saveJob(Job job, Employer employer) {

        job.setPostedDate(LocalDateTime.now());

        if (job.getStatus() == null) {
            job.setStatus("ACTIVE");
        }

        // ⭐ VERY IMPORTANT FIX (Dashboard Issue)
        job.setEmployer(employer);

        return jobRepository.save(job);
    }

    // =========================
    // REST POST JOB (JobDto)
    // =========================
    public Job postJob(JobDto dto, Employer employer) {

        Job job = new Job();

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setJobType(dto.getJobType());
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());

        job.setPostedDate(LocalDateTime.now());
        job.setStatus("ACTIVE");

        // ⭐ MOST IMPORTANT LINE
        job.setEmployer(employer);

        return jobRepository.save(job);
    }

    // =========================
    // EDIT JOB
    // =========================
    public Job editJob(Long id, JobDto dto) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setJobType(dto.getJobType());
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setUpdatedAt(LocalDateTime.now());

        return jobRepository.save(job);
    }

    // =========================
    // DELETE JOB
    // =========================
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    // =========================
    // STATUS UPDATES
    // =========================
    public Job closeJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("CLOSED");
        return jobRepository.save(job);
    }

    public Job reopenJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("ACTIVE");
        return jobRepository.save(job);
    }

    public Job markFilled(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("FILLED");
        return jobRepository.save(job);
    }

    // =========================
    // DASHBOARD HELPERS
    // =========================
    public long countActiveJobs(Employer employer) {
        return jobRepository.findByEmployer(employer)
                .stream()
                .filter(j -> "ACTIVE".equals(j.getStatus()))
                .count();
    }

    public List<Job> getJobsByEmployer(Employer employer) {
        return jobRepository.findByEmployer(employer);
    }

    // =========================
    // SEARCH FEATURES
    // =========================
    public List<Job> searchByRole(String title) {
        return jobRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Job> searchByLocation(String location) {
        return jobRepository.findByLocationContainingIgnoreCase(location);
    }

    public List<Job> searchByExperience(String exp) {
        return jobRepository.findByExperienceRequiredContainingIgnoreCase(exp);
    }

    public List<Job> searchBySalary(Double salary) {
        return jobRepository.findBySalaryMinGreaterThanEqual(salary);
    }

    public List<Job> searchByJobType(String type) {
        return jobRepository.findByJobTypeIgnoreCase(type);
    }

    // =========================
    // GET ALL JOBS / DETAILS
    // =========================
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }
}