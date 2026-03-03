package com.revhire.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.revhire.service.ApplicationService;
import com.revhire.service.JobService;
import com.revhire.model.Application;
import com.revhire.model.Job;

import java.security.Principal;
import java.util.List;

import org.springframework.ui.Model;

@Controller

@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private JobService jobService;

    // ================= APPLY =================
    @PostMapping("/apply")
    // @GetMapping("/apply")
    public String applyJob(@RequestParam Long jobId,
            @RequestParam Long jobSeekerId) {

        System.out.println("Applying for Job ID: " + jobId + " by Seeker ID: " + jobSeekerId);
        applicationService.applyJob(jobId, jobSeekerId);

        return "redirect:/applications/jobseeker/" + jobSeekerId;
    }

    // ================= WITHDRAW =================
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam Long applicationId,
            @RequestParam String notes,
            @RequestParam Long jobSeekerId) {

        applicationService.withdrawApplication(applicationId, notes);

        return "redirect:/applications/jobseeker/" + jobSeekerId;
    }

    // ================= SHORTLIST =================
    @PostMapping("/shortlist")
    public String shortlist(
            @RequestParam Long applicationId,
            @RequestParam Long jobId,
            @RequestParam String notes) {

        applicationService.shortlistCandidate(applicationId, notes);
        return "redirect:/employer/applicant/" + applicationId;
    }

    // ================= REJECT =================
    @PostMapping("/reject")
    public String reject(@RequestParam Long applicationId,
            @RequestParam Long jobId,
            @RequestParam String notes) {

        applicationService.rejectCandidate(applicationId, notes);
        return "redirect:/employer/applicant/" + applicationId;
    }

    // ================= BULK UPDATE =================
    @PostMapping("/bulk-update")
    public String bulkUpdate(
            @RequestParam(value = "applicationIds", required = false) List<Long> applicationIds,

            @RequestParam(value = "status", required = false) String status,

            @RequestParam(value = "bulkNote", required = false) String bulkNote,

            RedirectAttributes redirectAttributes) {

        if (status == null || status.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a bulk action.");
            return "redirect:/employer/dashboard";
        }

        if (applicationIds == null || applicationIds.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select at least one applicant.");
            return "redirect:/employer/dashboard";
        }

        Application.ApplicationStatus newStatus = Application.ApplicationStatus.valueOf(status);

        applicationService.bulkUpdateStatus(
                applicationIds, newStatus, bulkNote);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Bulk update successful.");

        return "redirect:/employer/dashboard";
    }

    // ================= ADD NOTES =================
    @PostMapping("/add-notes")
    public String addNotes(@RequestParam Long applicationId,
            @RequestParam String notes,
            @RequestParam Long jobId) {

        applicationService.addNotes(applicationId, notes);
        return "redirect:/employer/applicant/" + applicationId;
    }

    // ================= VIEW JOB SEEKER APPLICATIONS =================
    @GetMapping("/jobseeker/{jobSeekerId}")
    public String viewApplicationsByJobSeeker(
            @PathVariable Long jobSeekerId,
            Model model) {

        model.addAttribute("applications",
                applicationService.getApplicationsByJobSeeker(jobSeekerId));

        model.addAttribute("jobSeekerId", jobSeekerId);

        return "jobseeker/applications";
    }

    // ================= VIEW JOB APPLICANTS =================
    @GetMapping("/job/{jobId}")
    public String viewApplicationsByJob(
            @PathVariable Long jobId,
            Model model) {

        model.addAttribute("applications",
                applicationService.getApplicationsByJob(jobId));

        model.addAttribute("jobId", jobId);

        return "employer/applicants";
    }

    @GetMapping("/apply/{jobId}")
    public String showApplyPage(@PathVariable Long jobId, Model model) {

        Job job = jobService.getJobById(jobId);

        model.addAttribute("job", job);

        return "jobseeker/apply-job";
    }

    @PostMapping("/submit")
    public String submitApplication(
            @RequestParam Long jobId,
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            @RequestParam(required = false) String coverLetter,
            Principal principal) {

        applicationService.apply(jobId, resume, coverLetter, principal.getName());

        return "redirect:/jobseeker/dashboard?applied";
    }
}