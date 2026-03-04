package com.revhire.service;

import com.revhire.dto.JobDto;
import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.repository.JobRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobAlertService jobAlertService;

    public JobService(JobRepository jobRepository, JobAlertService jobAlertService) {
        this.jobRepository = jobRepository;
        this.jobAlertService = jobAlertService;
    }

    // =========================
    // SAVE JOB
    // =========================
    public Job saveJob(Job job, Employer employer) {
        job.setPostedDate(LocalDateTime.now());
        if (job.getStatus() == null) {
            job.setStatus("ACTIVE");
        }
        job.setEmployer(employer);

        if (job.getNumberOfOpenings() == null) {
            job.setNumberOfOpenings(1);
        }

        Job savedJob = jobRepository.save(job);

        if (jobAlertService != null) {
            jobAlertService.checkNewJobAgainstAlerts(savedJob);
        }

        return savedJob;
    }

    // =========================
    // POST JOB (DTO)
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
    // STATUS MANAGEMENT
    // =========================
    public Job closeJob(Long id) {
        Job job = getJobById(id);
        job.setStatus("CLOSED");
        return jobRepository.save(job);
    }

    public Job reopenJob(Long id) {
        Job job = getJobById(id);
        job.setStatus("ACTIVE");
        return jobRepository.save(job);
    }

    public Job markFilled(Long id) {
        Job job = getJobById(id);
        job.setStatus("FILLED");
        return jobRepository.save(job);
    }

    // =========================
    // DASHBOARD HELPERS
    // =========================
    public List<Job> getJobsByEmployer(Employer employer) {
        return jobRepository.findByEmployer(employer);
    }

    public long countActiveJobs(Employer employer) {
        return getJobsByEmployer(employer)
                .stream()
                .filter(j -> "ACTIVE".equals(j.getStatus()))
                .count();
    }

    // =========================
    // SEARCH
    // =========================
    public List<Job> searchByRole(String title) {
        return jobRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Job> searchByTitle(String title) {
        return jobRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Job> searchByLocation(String location) {
        return jobRepository.findByLocationContainingIgnoreCase(location);
    }

    public List<Job> searchBySalary(Double salary) {
        return jobRepository.findBySalaryMinGreaterThanEqual(salary);
    }

    public List<Job> searchByJobType(String type) {
        return jobRepository.findByJobTypeIgnoreCase(type);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    public Job updateJob(Long id, Job updatedJob) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setTitle(updatedJob.getTitle());
        job.setDescription(updatedJob.getDescription());
        job.setLocation(updatedJob.getLocation());
        job.setJobType(updatedJob.getJobType());
        job.setExperienceRequired(updatedJob.getExperienceRequired());
        job.setSalaryMin(updatedJob.getSalaryMin());
        job.setSalaryMax(updatedJob.getSalaryMax());

        return jobRepository.save(job);
    }

    public List<Job> advancedSearch(String title, String location, String company,
            String jobType, Double minSalary, Double maxSalary,
            Integer daysPosted, Integer minExp, Integer maxExp) {

        List<Job> results = jobRepository.advancedSearch(
                title, location, company, jobType, minSalary, maxSalary, "ACTIVE");

        if (daysPosted != null && daysPosted > 0) {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysPosted);
            results = results.stream()
                    .filter(j -> j.getPostedDate() != null && j.getPostedDate().isAfter(cutoffDate))
                    .collect(Collectors.toList());
        }

        if (minExp != null || maxExp != null) {
            results = results.stream()
                    .filter(j -> {
                        try {
                            if (j.getExperienceRequired() == null)
                                return true;
                            String expStr = j.getExperienceRequired().replaceAll("[^0-9-]", "");
                            if (expStr.contains("-")) {
                                String[] parts = expStr.split("-");
                                int jobMinExp = Integer.parseInt(parts[0].trim());
                                int jobMaxExp = Integer.parseInt(parts[1].trim());

                                boolean minMatch = minExp == null || jobMaxExp >= minExp;
                                boolean maxMatch = maxExp == null || jobMinExp <= maxExp;
                                return minMatch && maxMatch;
                            } else {
                                int jobExp = Integer.parseInt(expStr);
                                return (minExp == null || jobExp >= minExp) &&
                                        (maxExp == null || jobExp <= maxExp);
                            }
                        } catch (Exception e) {
                            return true;
                        }
                    })
                    .collect(Collectors.toList());
        }

        return results;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void closeExpiredJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<Job> expiredJobs = jobRepository.findByDeadlineBeforeAndStatus(now, "ACTIVE");

        for (Job job : expiredJobs) {
            job.setStatus("CLOSED");
            jobRepository.save(job);
        }
    }
}