package com.revhire.controller;

import com.revhire.model.Job;
import com.revhire.service.JobService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // JOB DETAILS PAGE
    @GetMapping("/{id}")
    public Job getJob(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    // ADVANCED SEARCH
    @GetMapping("/search")
    public List<Job> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Double salaryMin,
            @RequestParam(required = false) Double salaryMax,
            @RequestParam(required = false) String datePosted
    ) {

        LocalDate date =
                datePosted != null ? LocalDate.parse(datePosted) : null;

        return jobService.searchJobsAdvanced(
                title, location, experience,
                jobType, companyName,
                salaryMin, salaryMax, date
        );
    }
}