package com.revhire.controller;

import com.revhire.dto.JobDto;
import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.service.EmployerService;
import com.revhire.service.JobService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

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

    // =========================
    // WEB: SAVE JOB (Thymeleaf Form)
    // =========================
    @PostMapping("/save")
    public String saveJob(@ModelAttribute Job job,
                          Authentication authentication) {

        String email = authentication.getName();
        Employer employer = employerService.getEmployerByEmail(email);

        jobService.saveJob(job, employer);

        return "redirect:/employer/manage-jobs";
    }

    // =========================
    // REST: POST JOB (JSON)
    // =========================
    @PostMapping
    @ResponseBody
    public Job postJob(@RequestBody JobDto dto,
                       Authentication authentication) {

        String email = authentication.getName();
        Employer employer = employerService.getEmployerByEmail(email);

        return jobService.postJob(dto, employer);
    }

    // =========================
    // REST: EDIT JOB
    // =========================
    @PutMapping("/{id}")
    @ResponseBody
    public Job editJob(@PathVariable Long id,
                       @RequestBody JobDto dto) {
        return jobService.editJob(id, dto);
    }

    // =========================
    // REST: DELETE JOB
    // =========================
    @DeleteMapping("/{id}")
    @ResponseBody
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
    }

    // =========================
    // STATUS UPDATES
    // =========================
    @PutMapping("/{id}/close")
    @ResponseBody
    public Job closeJob(@PathVariable Long id) {
        return jobService.closeJob(id);
    }

    @PutMapping("/{id}/reopen")
    @ResponseBody
    public Job reopenJob(@PathVariable Long id) {
        return jobService.reopenJob(id);
    }

    @PutMapping("/{id}/filled")
    @ResponseBody
    public Job markFilled(@PathVariable Long id) {
        return jobService.markFilled(id);
    }

    // =========================
    // SEARCH PAGE (WEB)
    // =========================
    @GetMapping("/search-page")
    public String searchJobsPage(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double salary,
            Model model) {

        List<Job> jobs = jobService.getAllJobs();

        if (title != null && !title.isEmpty()) {
            jobs = jobService.searchByRole(title);
        }

        if (location != null && !location.isEmpty()) {
            jobs = jobService.searchByLocation(location);
        }

        if (salary != null) {
            jobs = jobService.searchBySalary(salary);
        }

        model.addAttribute("jobs", jobs);

        return "jobseeker/search-jobs";
    }

    // =========================
    // SEARCH APIs
    // =========================
    @GetMapping("/search/role")
    @ResponseBody
    public List<Job> searchByRole(@RequestParam String title) {
        return jobService.searchByRole(title);
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

    // =========================
    // GET ALL JOBS
    // =========================
    @GetMapping("/all")
    @ResponseBody
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    // =========================
    // OPEN POST JOB PAGE
    // =========================
    @GetMapping("/post")
    public String openPostJobPage(Model model) {
        model.addAttribute("jobDto", new JobDto());
        return "employer/post-job";
    }

    // =========================
    // EMPLOYER VIEW JOB DETAILS
    // =========================
    @GetMapping("/{id}")
    public String viewEmployerJob(@PathVariable Long id, Model model) {
        model.addAttribute("job", jobService.getJobById(id));
        return "employer/job-details";
    }

    // =========================
    // JOB SEEKER VIEW JOB DETAILS
    // =========================
    @GetMapping("/view/{id}")
    public String viewJobDetails(@PathVariable Long id,
                                 Model model) {

        model.addAttribute("job", jobService.getJobById(id));
        return "jobseeker/job-details";
    }
}