//package com.revhire.controller;
//
//import com.revhire.dto.JobDto;
//import com.revhire.model.Employer;
//import com.revhire.model.Job;
//import com.revhire.service.EmployerService;
//import com.revhire.service.JobService;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.ui.Model;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/jobs")
//public class JobController {
//
//    private final JobService jobService;
//    private final EmployerService employerService;
//
//    public JobController(JobService jobService,
//                         EmployerService employerService) {
//        this.jobService = jobService;
//        this.employerService = employerService;
//    }
//
//    // =========================
//    // WEB: POST JOB (Thymeleaf Form)
//    // =========================
//    @PostMapping("/save")
//    public String saveJob(@ModelAttribute Job job,
//                          Authentication authentication) {
//
//        String email = authentication.getName();
//        Employer employer = employerService.getEmployerByEmail(email);
//        job.setEmployer(employer);
//        jobService.saveJob(job);
//
//        return "redirect:/employer/manage-jobs"; // redirect after form submit
//    }
//
//    // =========================
//    // REST: POST JOB (JSON)
//    // =========================
//    @PostMapping
//    @ResponseBody
//    public Job postJob(@RequestBody JobDto dto) {
//        return jobService.postJob(dto);
//    }
//
//    // =========================
//    // REST: EDIT JOB
//    // =========================
//    @PutMapping("/{id}")
//    @ResponseBody
//    public Job editJob(@PathVariable Long id,
//                       @RequestBody JobDto dto) {
//        return jobService.editJob(id, dto);
//    }
//
//    // =========================
//    // REST: DELETE JOB
//    // =========================
//    @DeleteMapping("/{id}")
//    @ResponseBody
//    public void deleteJob(@PathVariable Long id) {
//        jobService.deleteJob(id);
//    }
//
//    // =========================
//    // REST: CLOSE JOB
//    // =========================
//    @PutMapping("/{id}/close")
//    @ResponseBody
//    public Job closeJob(@PathVariable Long id) {
//        return jobService.closeJob(id);
//    }
//
//    // =========================
//    // REST: REOPEN JOB
//    // =========================
//    @PutMapping("/{id}/reopen")
//    @ResponseBody
//    public Job reopenJob(@PathVariable Long id) {
//        return jobService.reopenJob(id);
//    }
//
//    // =========================
//    // REST: MARK FILLED JOB
//    // =========================
//    @PutMapping("/{id}/filled")
//    @ResponseBody
//    public Job markFilled(@PathVariable Long id) {
//        return jobService.markFilled(id);
//    }
//
//    // =========================
//    // REST: SEARCH JOBS
//    // =========================
//    @GetMapping("/search-page")
//    public String searchJobsPage(Model model) {
//
//        // FETCH ALL JOBS FROM DB
//        List<Job> jobs = jobService.getAllJobs();
//
//        // SEND TO FRONTEND
//        model.addAttribute("jobs", jobs);
//
//        // RETURN HTML PAGE
//        return "jobseeker/search-jobs";
//    }
//    @GetMapping("/search/role")
//    @ResponseBody
//    public List<Job> searchByRole(@RequestParam String title) {
//        return jobService.searchByRole(title);
//    }
//
//    @GetMapping("/search/location")
//    @ResponseBody
//    public List<Job> searchByLocation(@RequestParam String location) {
//        return jobService.searchByLocation(location);
//    }
//
//    @GetMapping("/search/experience")
//    @ResponseBody
//    public List<Job> searchByExperience(@RequestParam String experience) {
//        return jobService.searchByExperience(experience);
//    }
//
//    @GetMapping("/search/salary")
//    @ResponseBody
//    public List<Job> searchBySalary(@RequestParam Double salary) {
//        return jobService.searchBySalary(salary);
//    }
//
//    @GetMapping("/search/type")
//    @ResponseBody
//    public List<Job> searchByJobType(@RequestParam String jobType) {
//        return jobService.searchByJobType(jobType);
//    }
//
//    // =========================
//    // WEB / REST: GET ALL JOBS
//    // =========================
//    @GetMapping("/all")
//    @ResponseBody
//    public List<Job> getAllJobs() {
//        return jobService.getAllJobs();
//    }
//
//    // =========================
//    // WEB / REST: GET JOB DETAILS
//    // =========================
//    @GetMapping("/{id}/details")
//    @ResponseBody
//    public Job getJob(@PathVariable Long id) {
//        return jobService.getJobById(id);
//    }
//}


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
    // WEB: POST JOB (Thymeleaf Form)
    // =========================
    @PostMapping("/save")
    public String saveJob(@ModelAttribute Job job,
                          Authentication authentication) {

        String email = authentication.getName();
        Employer employer = employerService.getEmployerByEmail(email);

        // ⭐ FIXED
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

        // ⭐ FIXED
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
            jobs = jobs.stream()
                    .filter(j -> j.getLocation() != null &&
                            j.getLocation().toLowerCase().contains(location.toLowerCase()))
                    .toList();
        }

        if (salary != null) {
            jobs = jobs.stream()
                    .filter(j -> j.getSalaryMin() != null &&
                            j.getSalaryMin() >= salary)
                    .toList();
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

    @GetMapping("/search/experience")
    @ResponseBody
    public List<Job> searchByExperience(@RequestParam String experience) {
        return jobService.searchByExperience(experience);
    }

    @GetMapping("/search/salary")
    @ResponseBody
    public List<Job> searchBySalary(@RequestParam Double salary) {
        return jobService.searchBySalary(salary);
    }

    @GetMapping("/search/type")
    @ResponseBody
    public List<Job> searchByJobType(@RequestParam String jobType) {
        return jobService.searchByJobType(jobType);
    }

    // =========================
    // GET ALL JOBS
    // =========================
    @GetMapping("/all")
    @ResponseBody
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }
    
    @GetMapping("/post")
    public String openPostJobPage(Model model) {

        model.addAttribute("jobDto", new JobDto());

        return "employer/post-job";
    }
    // =========================
    // JOB DETAILS PAGE (WEB)
    // =========================
    @GetMapping("/{id}")
    public String viewJob(@PathVariable Long id, Model model) {

        Job job = jobService.getJobById(id);

        model.addAttribute("job", job);

        return "employer/job-details";
    }
    
    @GetMapping("/view/{id}")
    public String viewJobDetails(@PathVariable Long id,
                                 Model model) {

        model.addAttribute("job",
                jobService.getJobById(id));

        return "job-details";
    }
}