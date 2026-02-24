package com.revhire.controller;

import com.revhire.model.Application;
import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.model.User;
import com.revhire.service.ApplicationService;
import com.revhire.service.EmployerService;
import com.revhire.service.JobService;
import com.revhire.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    @Autowired
    private ApplicationService applicationService;

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
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            Authentication authentication,
                            HttpSession session) {

        if (authentication == null) {
            return "redirect:/auth/login";
        }

        String email = authentication.getName();

        User user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/auth/login";
        }

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());

        model.addAttribute("user", user);

        Employer employer = employerService.getEmployerByEmail(email);
        if (employer == null) {
            return "redirect:/auth/login";
        }

        // Get employer jobs
        List<Job> jobs = jobService.getJobsByEmployer(employer);

        // Collect all applications
        List<Application> allApplications = new ArrayList<>();

        for (Job job : jobs) {
            List<Application> jobApplications =
                    applicationService.getApplicationsByJob(job.getJobId());

            if (jobApplications != null) {
                allApplications.addAll(jobApplications);
            }
        }

        // Send to frontend
        model.addAttribute("applications", allApplications);

        // Active jobs count
        long activeJobs = jobs.stream()
                .filter(j -> "ACTIVE".equals(j.getStatus()))
                .count();

        model.addAttribute("activeJobs", activeJobs);

        // Total applicants
        model.addAttribute("totalApplicants", allApplications.size());

        // Shortlisted count
        long shortlistedCount = allApplications.stream()
                .filter(app ->
                        app.getStatus() == Application.ApplicationStatus.SHORTLISTED)
                .count();

        model.addAttribute("shortlisted", shortlistedCount);

        return "employer/dashboard";
    }

    // =========================
    // SHORTLIST
    // =========================
    @PostMapping("/shortlist/{applicationId}")
    public String shortlist(
            @PathVariable Long applicationId,
            @RequestParam String notes) {

        applicationService.shortlistCandidate(applicationId, notes);

        return "redirect:/employer/dashboard";
    }

    // =========================
    // REJECT
    // =========================
    @PostMapping("/reject/{applicationId}")
    public String reject(@PathVariable Long applicationId,
                         @RequestParam String notes) {

        applicationService.rejectCandidate(applicationId, notes);
        return "redirect:/employer/dashboard";
    }

    // =========================
    // MANAGE JOBS
    // =========================
    @GetMapping("/manage-jobs")
    public String manageJobs(Model model,
                             Authentication authentication) {

        Employer employer =
                employerService.getEmployerByEmail(authentication.getName());

        model.addAttribute("jobs",
                jobService.getJobsByEmployer(employer));

        return "employer/manage-jobs";
    }

    // =========================
    // COMPANY PROFILE
    // =========================
    @GetMapping("/company-profile")
    public String companyProfile(Model model,
                                 Authentication authentication) {

        Employer employer =
                employerService.getEmployerByEmail(authentication.getName());

        model.addAttribute("user", employer);

        return "employer/company-profile";
    }
    
    //bulk update
    @PostMapping("/bulk-update")
    public String bulkUpdate(
            @RequestParam(value = "applicationIds", required = false)
            List<Long> applicationIds,

            @RequestParam(value = "status", required = false)
            String status,

            @RequestParam(value = "bulkNote", required = false)
            String bulkNote,

            RedirectAttributes redirectAttributes) {

        if (status == null || status.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select bulk action.");
            return "redirect:/employer/dashboard";
        }

        if (applicationIds == null || applicationIds.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select at least one applicant.");
            return "redirect:/employer/dashboard";
        }

        Application.ApplicationStatus newStatus =
                Application.ApplicationStatus.valueOf(status);

        applicationService.bulkUpdateStatus(
                applicationIds,
                newStatus,
                bulkNote);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Bulk update successful.");

        return "redirect:/employer/dashboard";
    }
    
}