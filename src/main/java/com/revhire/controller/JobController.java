package com.revhire.controller;

import com.revhire.dto.JobDto;
import com.revhire.model.Job;
import com.revhire.service.JobService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // POST JOB
    @PostMapping
    public Job postJob(@RequestBody JobDto dto) {
        return jobService.postJob(dto);
    }

    // EDIT JOB
    @PutMapping("/{id}")
    public Job editJob(@PathVariable Long id,
                       @RequestBody JobDto dto) {
        return jobService.editJob(id, dto);
    }

    // DELETE JOB
    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
    }

    // CLOSE JOB
    @PutMapping("/{id}/close")
    public Job closeJob(@PathVariable Long id) {
        return jobService.closeJob(id);
    }

    // REOPEN JOB
    @PutMapping("/{id}/reopen")
    public Job reopenJob(@PathVariable Long id) {
        return jobService.reopenJob(id);
    }

    // FILLED JOB
    @PutMapping("/{id}/filled")
    public Job markFilled(@PathVariable Long id) {
        return jobService.markFilled(id);
    }

    // SEARCH APIs

    @GetMapping("/search/role")
    public List<Job> searchByRole(@RequestParam String title) {
        return jobService.searchByRole(title);
    }

    @GetMapping("/search/location")
    public List<Job> searchByLocation(@RequestParam String location) {
        return jobService.searchByLocation(location);
    }

    @GetMapping("/search/experience")
    public List<Job> searchByExperience(@RequestParam String experience) {
        return jobService.searchByExperience(experience);
    }

    @GetMapping("/search/salary")
    public List<Job> searchBySalary(@RequestParam Double salary) {
        return jobService.searchBySalary(salary);
    }

    @GetMapping("/search/type")
    public List<Job> searchByJobType(@RequestParam String jobType) {
        return jobService.searchByJobType(jobType);
    }
}