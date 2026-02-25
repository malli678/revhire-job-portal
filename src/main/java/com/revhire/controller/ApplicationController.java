package com.revhire.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.revhire.service.ApplicationService;
import com.revhire.model.Application;

import java.util.List;

import org.springframework.ui.Model;

@Controller


@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // ================= APPLY =================
    @PostMapping("/apply")
   // @GetMapping("/apply")
    public String applyJob(@RequestParam Long jobId,
                           @RequestParam Long jobSeekerId) {

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

        return "redirect:/applications/job/" + jobId;
    }

    // ================= REJECT =================
    @PostMapping("/reject")
    public String reject(@RequestParam Long applicationId,
                         @RequestParam Long jobId,
                         @RequestParam String notes) {

        applicationService.rejectCandidate(applicationId, notes);

        return "redirect:/applications/job/" + jobId;
    }

    // ================= BULK UPDATE =================
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
                    "Please select a bulk action.");
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

        return "redirect:/applications/job/" + jobId;
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
}