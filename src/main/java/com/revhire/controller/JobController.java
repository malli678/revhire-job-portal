package com.revhire.controller;

import com.revhire.dto.JobDto;
import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.service.EmployerService;
import com.revhire.service.JobService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    public String saveJob(@ModelAttribute JobDto jobDto,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {

        try {
            String email = authentication.getName();
            Employer employer = employerService.getEmployerByEmail(email);

            Job job = new Job();
            job.setTitle(jobDto.getTitle());
            job.setDescription(jobDto.getDescription());
            job.setLocation(jobDto.getLocation());
            job.setJobType(jobDto.getJobType());
            job.setExperienceRequired(jobDto.getExperienceRequired());
            job.setSkillsRequired(jobDto.getSkillsRequired());
            job.setEducationRequired(jobDto.getEducationRequired());
            job.setSalaryMin(jobDto.getSalaryMin());
            job.setSalaryMax(jobDto.getSalaryMax());
            job.setDeadline(jobDto.getDeadline());
            job.setNumberOfOpenings(jobDto.getNumberOfOpenings());

            jobService.saveJob(job, employer);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Job posted successfully!");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to post job: " + e.getMessage());
        }

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
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) Integer daysPosted,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false) Integer maxExp,
            Model model) {

        List<Job> jobs =
                jobService.advancedSearch(title, location, company, jobType,
                        minSalary, maxSalary, daysPosted, minExp, maxExp);

        model.addAttribute("jobs", jobs);
        model.addAttribute("role", "JOBSEEKER");

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
    public String viewEmployerJob(@PathVariable Long id,
                                  Model model) {

        model.addAttribute("job",
                jobService.getJobById(id));

        return "employer/job-details";
    }

    // =========================
    // JOB SEEKER VIEW JOB DETAILS
    // =========================
    @GetMapping("/view/{id}")
    public String viewJobDetails(@PathVariable Long id,
                                 Model model) {

        model.addAttribute("job",
                jobService.getJobById(id));

        return "jobseeker/job-details";
    }

    // =========================
    // ADVANCED SEARCH API
    // =========================
    @GetMapping("/advanced-search")
    @ResponseBody
    public List<Job> advancedSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) Integer daysPosted,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false) Integer maxExp) {

        return jobService.advancedSearch(
                title,
                location,
                company,
                jobType,
                minSalary,
                maxSalary,
                daysPosted,
                minExp,
                maxExp);
    }
}