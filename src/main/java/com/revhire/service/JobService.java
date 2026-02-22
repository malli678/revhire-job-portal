package com.revhire.service;

import com.revhire.dto.JobDto;
import com.revhire.model.Job;
import com.revhire.repository.JobRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // POST JOB
    public Job postJob(JobDto dto) {

        Job job = new Job();
        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setJobType(dto.getJobType());
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());

        return jobRepository.save(job);
    }

    // EDIT JOB
    public Job editJob(Long id, JobDto dto) {

        Job job = jobRepository.findById(id).orElseThrow();

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setJobType(dto.getJobType());
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());

        return jobRepository.save(job);
    }

    // DELETE JOB
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    // CLOSE JOB
    public Job closeJob(Long id) {
        Job job = jobRepository.findById(id).orElseThrow();
        job.setStatus("CLOSED");
        return jobRepository.save(job);
    }

    // REOPEN JOB
    public Job reopenJob(Long id) {
        Job job = jobRepository.findById(id).orElseThrow();
        job.setStatus("ACTIVE");
        return jobRepository.save(job);
    }

    // MARK FILLED
    public Job markFilled(Long id) {
        Job job = jobRepository.findById(id).orElseThrow();
        job.setStatus("FILLED");
        return jobRepository.save(job);
    }

    // SEARCH FEATURES
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
}