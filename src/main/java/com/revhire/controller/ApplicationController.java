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

    // ================= ADD NOTES =================
    @PostMapping("/add-notes")
    public String addNotes(@RequestParam(required = false) Long applicationId,
            @RequestParam String notes,
            @RequestParam(required = false) Long jobId,
            @RequestHeader(value = "Referer", required = false) String referer) {

        if (applicationId == null) {
            System.err.println("CRITICAL: addNotes called with null applicationId");
            if (jobId != null) {
                return "redirect:/applications/job/" + jobId;
            }
            return "redirect:/employer/dashboard";
        }

        applicationService.addNotes(applicationId, notes);

        if (referer != null && !referer.contains("/applicant/")) {
            return "redirect:" + referer;
        }
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
            Principal principal,
            RedirectAttributes ra) {

        try {
            applicationService.apply(jobId, resume, coverLetter, principal.getName());
            ra.addFlashAttribute("successMessage", "Application submitted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/jobseeker/job/" + jobId;
        }

        return "redirect:/jobseeker/dashboard?applied";
    }
}