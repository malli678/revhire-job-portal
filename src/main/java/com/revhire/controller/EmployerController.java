package com.revhire.controller;

import com.revhire.dto.CompanyProfileUpdateDto;
import com.revhire.model.Application;
import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.model.User;
import com.revhire.service.ApplicationService;
import com.revhire.service.EmployerService;
import com.revhire.service.JobService;
import com.revhire.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    private final ApplicationService applicationService;
    private final UserService userService;
    private final JobService jobService;
    private final EmployerService employerService;

    public EmployerController(ApplicationService applicationService,
                              UserService userService,
                              JobService jobService,
                              EmployerService employerService) {
        this.applicationService = applicationService;
        this.userService = userService;
        this.jobService = jobService;
        this.employerService = employerService;
    }

    // =========================
    // DASHBOARD ⭐ OPTIMIZED
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            Authentication authentication,
                            HttpSession session) {

        String email = authentication.getName();

        Employer employer = employerService.getEmployerByEmail(email);

        // ✅ Session Data
        session.setAttribute("userId", employer.getUserId());
        session.setAttribute("userName", employer.getFullName());
        session.setAttribute("userRole", employer.getRole().name());

        model.addAttribute("user", employer);

        // ✅ Fetch ALL employer applications (NO DUPLICATES)
        List<Application> allApplications =
                employerService.getApplicationsForEmployer(employer);

        // ✅ Dashboard Statistics ⭐⭐⭐
        model.addAttribute("totalJobs",
                employerService.countTotalJobs(employer));

        model.addAttribute("activeJobs",
                employerService.countActiveJobs(employer));

        model.addAttribute("totalApplications",
                allApplications.size());

        model.addAttribute("totalApplicants",
                allApplications.size());   // (can refine later)

        model.addAttribute("shortlisted",
                employerService.countByStatus(
                        employer,
                        Application.ApplicationStatus.SHORTLISTED));

        model.addAttribute("rejected",
                employerService.countRejectedApplications(employer));

        model.addAttribute("pendingReviews",
                employerService.countPendingReviews(employer));

        // ✅ Applications Table
        model.addAttribute("applications", allApplications);

        return "employer/dashboard";
    }
    

    // =========================
    // FILTER APPLICANTS ⭐⭐⭐
    // =========================
    @GetMapping("/applicants/filter")
    public String filterApplicants(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) Integer experience,
                                   @RequestParam(required = false) String degree,
                                   @RequestParam(required = false) Integer days,
                                   Authentication authentication,
                                   Model model) {

        Employer employer = employerService.getEmployerByEmail(authentication.getName());

        List<Application> filtered;

        if (status != null && !status.isEmpty()) {

            filtered = applicationService.filterByStatus(
                    employer,
                    Application.ApplicationStatus.valueOf(status));

        } else if (experience != null) {

            filtered = applicationService.filterByExperience(employer, experience);

        } else if (degree != null && !degree.isEmpty()) {

            filtered = applicationService.filterByEducation(employer, degree);

        } else if (days != null) {

            LocalDateTime date = LocalDateTime.now().minusDays(days);
            filtered = applicationService.filterByDate(employer, date);

        } else {

            filtered = applicationService.getApplicationsForEmployer(employer);
        }

        model.addAttribute("applications", filtered);
        model.addAttribute("user", employer);

        // Keep dashboard cards visible
        model.addAttribute("activeJobs", employerService.countActiveJobs(employer));
        model.addAttribute("totalApplicants", employerService.countTotalApplications(employer));
        model.addAttribute("shortlisted",
                employerService.countByStatus(employer, Application.ApplicationStatus.SHORTLISTED));

        return "employer/dashboard";
    }

    // =========================
    // SHORTLIST
    // =========================
    @PostMapping("/shortlist/{applicationId}")
    public String shortlist(@PathVariable Long applicationId,
                            @RequestParam(required = false) String notes) {

        applicationService.shortlistCandidate(applicationId, notes);
        return "redirect:/employer/dashboard";
    }

    // =========================
    // REJECT
    // =========================
    @PostMapping("/reject/{applicationId}")
    public String reject(@PathVariable Long applicationId,
                         @RequestParam(required = false) String notes) {

        applicationService.rejectCandidate(applicationId, notes);
        return "redirect:/employer/dashboard";
    }

    // =========================
    // MANAGE JOBS PAGE
    // =========================
    @GetMapping("/manage-jobs")
    public String manageJobs(Model model, Authentication authentication) {

        Employer employer = employerService.getEmployerByEmail(authentication.getName());

        model.addAttribute("jobs",
        		jobService.getJobsByEmployer(employer));

        return "employer/manage-jobs";
    }

    // =========================
    // COMPANY PROFILE
    // =========================
    @GetMapping("/company-profile")
    public String companyProfile(Model model, Authentication authentication) {

        Employer employer = employerService.getEmployerByEmail(authentication.getName());

        model.addAttribute("user", employer);

        return "employer/company-profile";
    }

    // =========================
    // VIEW JOB DETAILS
    // =========================
    @GetMapping("/job/{id}")
    public String viewJob(@PathVariable Long id, Model model) {

        model.addAttribute("job",
                jobService.getJobById(id));

        return "employer/job-details";
    }

    // =========================
    // EDIT JOB PAGE
    // =========================
    @GetMapping("/job/edit/{id}")
    public String openEditJob(@PathVariable Long id, Model model) {

        Job job = jobService.getJobById(id);

        model.addAttribute("job", job);

        return "employer/edit-job";
    }
    @PostMapping("/job/close/{jobId}")
    public String closeJob(@PathVariable Long jobId) {

        System.out.println("CLOSE CLICKED → " + jobId);

        jobService.closeJob(jobId);
        return "redirect:/employer/manage-jobs";
    }
    @PostMapping("/job/delete/{jobId}")
    public String deleteJob(@PathVariable Long jobId) {

        System.out.println("DELETE CLICKED → " + jobId);

        jobService.deleteJob(jobId);
        return "redirect:/employer/manage-jobs";
    }
    @PostMapping("/job/reopen/{jobId}")
    public String reopenJob(@PathVariable Long jobId) {

        System.out.println("REOPEN CLICKED → " + jobId);

        jobService.reopenJob(jobId);

        return "redirect:/employer/manage-jobs";
    }
    // =========================
    // UPDATE JOB
    // =========================
    @PostMapping("/job/update/{id}")
    public String updateJobFromForm(@PathVariable Long id,
                                    @ModelAttribute Job job) {

        jobService.updateJob(id, job);   // ✅ FIXED

        return "redirect:/employer/manage-jobs";
    }

    // =========================
    // BULK UPDATE ⭐⭐⭐
    // =========================
    @PostMapping("/bulk-update")
    public String bulkUpdate(@RequestParam(required = false) List<Long> applicationIds,
                             @RequestParam String status,
                             @RequestParam(required = false) String bulkNote,
                             RedirectAttributes redirectAttributes) {

        if (applicationIds == null || applicationIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Select applicants first.");
            return "redirect:/employer/dashboard";
        }

        Application.ApplicationStatus newStatus =
                Application.ApplicationStatus.valueOf(status);

        applicationService.bulkUpdateStatus(applicationIds, newStatus, bulkNote);

        redirectAttributes.addFlashAttribute("successMessage", "Bulk update successful.");

        return "redirect:/employer/dashboard";
    }
    @PostMapping("/company-profile/update")
    public String updateCompanyProfile(@ModelAttribute CompanyProfileUpdateDto dto,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {

        String email = authentication.getName();

        Employer employer = employerService.getEmployerByEmail(email);

        employerService.updateCompanyProfile(employer.getUserId(), dto);

        redirectAttributes.addFlashAttribute("successMessage",
                "Company Profile Updated Successfully!");

        return "redirect:/employer/company-profile";
    }
    @GetMapping("/company-profile/edit")
    public String editCompanyProfile(Model model, Authentication authentication) {

        Employer employer = employerService
                .getEmployerByEmail(authentication.getName());

        CompanyProfileUpdateDto dto = new CompanyProfileUpdateDto();

        dto.setCompanyName(employer.getCompanyName());
        dto.setIndustry(employer.getIndustry());
        dto.setCompanySize(employer.getCompanySize());
        dto.setCompanyWebsite(employer.getCompanyWebsite());
        dto.setCompanyDescription(employer.getCompanyDescription());
        dto.setHeadquarters(employer.getHeadquarters());

        model.addAttribute("profileDto", dto);

        return "employer/company-profile-edit";
    }
  //filter employer
    @GetMapping("/applicants")
    public String applicantsPage(Model model,
                                 Authentication authentication,
                                 @RequestParam(required = false) String skill,
                                 @RequestParam(required = false) Integer experience,
                                 @RequestParam(required = false) String education,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String date) {

        Employer employer = employerService
                .getEmployerByEmail(authentication.getName());

        LocalDateTime appliedDate = null;

        if (date != null && !date.isBlank()) {
            appliedDate = LocalDateTime.parse(date);
        }

        List<Application> filteredApps =
                employerService.filterApplications(
                        employer,
                        skill,
                        experience,
                        education,
                        status,
                        appliedDate
                );

        model.addAttribute("applications", filteredApps);

        return "employer/applicants";
    }
}