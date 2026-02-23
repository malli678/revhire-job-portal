package com.revhire.controller;

import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.model.User;
import com.revhire.service.EmployerService;
import com.revhire.service.UserService;
import com.revhire.service.JobService;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    private final UserService userService;
    private final JobService jobService;
    private final EmployerService employerService;

    public EmployerController(UserService userService,
                              JobService jobService,
                              EmployerService employerService) {
        this.userService = userService;
        this.jobService = jobService;
        this.employerService = employerService;
    }

    // =========================
    // DASHBOARD  ⭐ FIXED
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            HttpSession session,
                            Authentication authentication) {

        if (authentication == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findByEmail(authentication.getName());

        if (user == null) {
            return "redirect:/auth/login";
        }

        // session values
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());

        // send user
        model.addAttribute("user", user);

        // ⭐ IMPORTANT FIX STARTS HERE
        Employer employer =
                employerService.getEmployerByEmail(authentication.getName());

        List<Job> employerJobs =
                jobService.getJobsByEmployer(employer);

        long activeJobs =
                employerJobs.stream()
                        .filter(j -> "ACTIVE".equals(j.getStatus()))
                        .count();

        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("totalApplicants", 0); // future feature
        model.addAttribute("shortlisted", 0);     // future feature
        // ⭐ IMPORTANT FIX ENDS HERE

        return "employer/dashboard";
    }

    // =========================
    // POST JOB API
    // =========================
    @PostMapping("/post-job")
    @ResponseBody
    public Job postJob(@RequestBody Job job,
                       Authentication auth) {

        Employer emp =
                employerService.getEmployerByEmail(auth.getName());

        job.setEmployer(emp);

        return employerService.postJob(job, emp);
    }

    // =========================
    // EDIT JOB
    // =========================
    @PutMapping("/job/{id}")
    @ResponseBody
    public Job editJob(@PathVariable Long id,
                       @RequestBody Job job) {
        return employerService.updateJob(id, job);
    }

    // =========================
    // DELETE JOB
    // =========================
    @DeleteMapping("/job/{id}")
    @ResponseBody
    public String delete(@PathVariable Long id) {
        employerService.deleteJob(id);
        return "Deleted Successfully";
    }

    // =========================
    // CLOSE JOB
    // =========================
    @PutMapping("/job/{id}/close")
    @ResponseBody
    public Job close(@PathVariable Long id) {
        return employerService.closeJob(id);
    }

    // =========================
    // REOPEN JOB
    // =========================
    @PutMapping("/job/{id}/reopen")
    @ResponseBody
    public Job reopen(@PathVariable Long id) {
        return employerService.reopenJob(id);
    }

    // =========================
    // MARK FILLED
    // =========================
    @PutMapping("/job/{id}/filled")
    @ResponseBody
    public Job filled(@PathVariable Long id) {
        return employerService.markFilled(id);
    }

    // =========================
    // Manage Jobs Page
    // =========================
    @GetMapping("/manage-jobs")
    public String manageJobs(Model model,
                             Authentication auth) {

        Employer employer =
                employerService.getEmployerByEmail(auth.getName());

        model.addAttribute("jobs",
                jobService.getJobsByEmployer(employer));

        return "employer/manage-jobs";
    }
    
    @GetMapping("/search-jobs")
    public String searchJobs(Model model) {

        // load all jobs
        model.addAttribute("jobs", jobService.getAllJobs());

        // ⭐ VERY IMPORTANT (ADD THIS)
        model.addAttribute("role", "EMPLOYER");

        return "jobseeker/search-jobs";
    }

    // =========================
    // POST JOB PAGE
    // =========================
    @GetMapping("/post-job-page")
    public String postJobPage() {
        return "employer/post-job";
    }
    
    //  company profile 
    @GetMapping("/company-profile")
    public String companyProfile(Model model, Authentication auth) {

        Employer employer =
                employerService.getEmployerByEmail(auth.getName());

        model.addAttribute("user", employer);

        return "employer/company-profile";
    }
    
    @GetMapping("/job/{id}")
    public String viewJob(@PathVariable Long id, Model model) {

        model.addAttribute("job", jobService.getJobById(id));

        return "employer/job-details";
    }
}