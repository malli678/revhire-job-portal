package com.revhire.controller;

import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.service.EmployerService;
import com.revhire.service.JobService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final EmployerService employerService;

    public JobController(JobService jobService,
                         EmployerService employerService) {
        this.jobService = jobService;
        this.employerService = employerService;
    }

    // POST JOB
    @PostMapping("/save")
    public String saveJob(@ModelAttribute Job job,
                          Authentication authentication) {

        String email = authentication.getName();
        Employer employer =
                employerService.getEmployerByEmail(email);

        job.setEmployer(employer);
        jobService.saveJob(job);

        return "redirect:/employer/dashboard";
    }

    // VIEW ALL JOBS (Job Search)
    @GetMapping("/all")
    @ResponseBody
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    // JOB DETAILS
    @GetMapping("/{id}")
    @ResponseBody
    public Job getJob(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    // DELETE JOB
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return "Job Deleted";
    }

    // CLOSE / REOPEN / FILLED
    @PutMapping("/status/{id}/{status}")
    @ResponseBody
    public Job updateStatus(@PathVariable Long id,
                            @PathVariable String status) {
        return jobService.updateStatus(id, status);
    }
}