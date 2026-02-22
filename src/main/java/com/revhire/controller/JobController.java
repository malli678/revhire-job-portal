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

    @PostMapping("/save")
    public String saveJob(@ModelAttribute Job job,
                          Authentication authentication) {

        String email = authentication.getName();
        Employer employer = employerService.getEmployerByEmail(email);
        job.setEmployer(employer);
        jobService.saveJob(job);

        return "redirect:/employer/manage-jobs";
    }

    @GetMapping("/all")
    @ResponseBody
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Job getJob(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return "Job Deleted Successfully";
    }

    @PutMapping("/status/{id}/{status}")
    @ResponseBody
    public Job updateStatus(@PathVariable Long id,
                            @PathVariable String status) {
        return jobService.updateStatus(id, status);
    }

    @GetMapping("/search/location")
    @ResponseBody
    public List<Job> searchByLocation(@RequestParam String location) {
        return jobService.searchByLocation(location);
    }

    @GetMapping("/search/title")
    @ResponseBody
    public List<Job> searchByTitle(@RequestParam String title) {
        return jobService.searchByTitle(title);
    }
}