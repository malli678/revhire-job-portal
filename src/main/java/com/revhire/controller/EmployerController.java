package com.revhire.controller;

import com.revhire.dto.CompanyProfileUpdateDto;
import com.revhire.model.Application;
import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.model.User;
import com.revhire.repository.ApplicationRepository;
import com.revhire.service.ApplicationService;
import com.revhire.service.EmployerService;
import com.revhire.service.JobService;
import com.revhire.service.UserService;

//import jakarta.annotation.Resource;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    private final ApplicationService applicationService;
    private final UserService userService;
    private final JobService jobService;
    private final EmployerService employerService;
    // Add to EmployerController.java fields
    @Autowired
    private ApplicationRepository applicationRepository;

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
    // Add to EmployerController.java

    @GetMapping("/dashboard")
    public String dashboard(Model model,
            Authentication authentication,
            HttpSession session) {

        String email = authentication.getName();
        Employer employer = employerService.getEmployerByEmail(email);

        // Session Data
        session.setAttribute("userId", employer.getUserId());
        session.setAttribute("userName", employer.getFullName());
        session.setAttribute("userRole", employer.getRole().name());

        model.addAttribute("user", employer);

        // Fetch ALL employer applications
        List<Application> allApplications = employerService.getApplicationsForEmployer(employer);

        // Dashboard Statistics
        model.addAttribute("totalJobs",
                employerService.countTotalJobs(employer));

        model.addAttribute("activeJobs",
                employerService.countActiveJobs(employer));

        model.addAttribute("totalApplications",
                allApplications.size());

        model.addAttribute("pendingReviews",
                employerService.countPendingReviews(employer));

        model.addAttribute("shortlisted",
                employerService.countByStatus(employer, Application.ApplicationStatus.SHORTLISTED));

        model.addAttribute("rejected",
                employerService.countRejectedApplications(employer));

        // ✅ NEW: Application statistics by status for charts
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("APPLIED", employerService.countByStatus(employer, Application.ApplicationStatus.APPLIED));
        statusCounts.put("UNDER_REVIEW",
                employerService.countByStatus(employer, Application.ApplicationStatus.UNDER_REVIEW));
        statusCounts.put("SHORTLISTED",
                employerService.countByStatus(employer, Application.ApplicationStatus.SHORTLISTED));
        statusCounts.put("REJECTED", employerService.countByStatus(employer, Application.ApplicationStatus.REJECTED));
        statusCounts.put("WITHDRAWN", employerService.countByStatus(employer, Application.ApplicationStatus.WITHDRAWN));

        model.addAttribute("statusCounts", statusCounts);

        // ✅ NEW: Applications per job for chart
        List<Job> jobs = jobService.getJobsByEmployer(employer);
        Map<String, Long> jobApplicationCounts = new LinkedHashMap<>();
        for (Job job : jobs) {
            long count = applicationRepository.countByJob_JobId(job.getJobId());
            jobApplicationCounts.put(job.getTitle(), count);
        }
        model.addAttribute("jobApplicationCounts", jobApplicationCounts);

        // ✅ NEW: Applications over time (last 7 days)
        Map<String, Long> applicationsOverTime = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            String dayLabel = date.format(java.time.format.DateTimeFormatter.ofPattern("EEE"));
            long count = allApplications.stream()
                    .filter(app -> app.getAppliedDate().toLocalDate().equals(date.toLocalDate()))
                    .count();
            applicationsOverTime.put(dayLabel, count);
        }
        model.addAttribute("applicationsOverTime", applicationsOverTime);

        // Applications Table
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

    @PostMapping("/job/filled/{jobId}")
    public String markJobAsFilled(@PathVariable Long jobId) {

        System.out.println("FILLED CLICKED → " + jobId);

        jobService.markFilled(jobId);

        return "redirect:/employer/manage-jobs";
    }

    // =========================
    // UPDATE JOB
    // =========================
    @PostMapping("/job/update/{id}")
    public String updateJobFromForm(@PathVariable Long id,
            @ModelAttribute Job job) {

        jobService.updateJob(id, job); // ✅ FIXED

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

        Application.ApplicationStatus newStatus = Application.ApplicationStatus.valueOf(status);

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

    // filter employer
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

        List<Application> filteredApps = employerService.filterApplications(
                employer,
                skill,
                experience,
                education,
                status,
                appliedDate);

        model.addAttribute("applications", filteredApps);

        return "employer/applicants";
    }

    @GetMapping("/public/{id}")
    public String viewPublicCompanyProfile(@PathVariable Long id, Model model) {
        Employer employer = employerService.getEmployerById(id);
        model.addAttribute("company", employer);
        model.addAttribute("activeJobs", jobService.getJobsByEmployer(employer)
                .stream()
                .filter(j -> "ACTIVE".equals(j.getStatus()))
                .count());
        return "employer/public-profile";
    }

    // Add to EmployerController.java

    @GetMapping("/search-by-resume")
    public String searchByResumeKeyword(@RequestParam String keyword, Model model,
            Authentication authentication) {
        Employer employer = employerService.getEmployerByEmail(authentication.getName());

        // Search applications where resume text contains keyword
        List<Application> applications = applicationRepository.findAll().stream()
                .filter(app -> app.getJob().getEmployer().equals(employer))
                .filter(app -> app.getJobSeeker().getResumeText() != null)
                .filter(app -> app.getJobSeeker().getResumeText().toLowerCase()
                        .contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        model.addAttribute("applications", applications);
        model.addAttribute("searchKeyword", keyword);

        return "employer/dashboard";
    }

    @PostMapping("/update-status/{applicationId}")
    public String updateStatus(@PathVariable Long applicationId,
            @RequestParam Application.ApplicationStatus status,
            @RequestParam(required = false) String notes) {
        applicationService.updateStatus(applicationId, status, notes);
        return "redirect:/employer/dashboard";
    }

    @PostMapping("/under-review/{applicationId}")
    public String moveToUnderReview(@PathVariable Long applicationId,
            @RequestParam(required = false) String notes) {
        applicationService.moveToUnderReview(applicationId, notes);
        return "redirect:/employer/dashboard";
    }

    @PostMapping("/shortlist-from-review/{applicationId}")
    public String shortlistFromReview(@PathVariable Long applicationId,
            @RequestParam(required = false) String notes) {
        applicationService.moveFromUnderReviewToShortlisted(applicationId, notes);
        return "redirect:/employer/dashboard";
    }

    // Add this method to EmployerController.java

    @GetMapping("/applicant/{applicationId}")
    public String viewApplicantDetails(@PathVariable Long applicationId,
            Model model,
            Authentication authentication) {
        try {
            Application application = applicationService.getApplicationById(applicationId);
            Employer employer = employerService.getEmployerByEmail(authentication.getName());

            // Verify this application belongs to this employer
            if (!application.getJob().getEmployer().getUserId().equals(employer.getUserId())) {
                throw new RuntimeException("Unauthorized access");
            }

            model.addAttribute("application", application);
            model.addAttribute("applicant", application.getJobSeeker());

            return "employer/applicant-details";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/employer/dashboard";
        }
    }

    @GetMapping("/download-resume/{applicationId}")
    public ResponseEntity<Resource> downloadApplicantResume(
            @PathVariable Long applicationId,
            Authentication authentication) {

        Application application = applicationService.getApplicationById(applicationId);
        Employer employer = employerService.getEmployerByEmail(authentication.getName());

        // Security check
        if (!application.getJob().getEmployer().getUserId()
                .equals(employer.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String resumeFileName = application.getResumePath();
        if (resumeFileName == null) {
            resumeFileName = application.getJobSeeker().getResumeFile();
        }

        Path path = Paths.get("uploads").resolve(resumeFileName);
        Resource resource = new FileSystemResource(path.toFile());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}