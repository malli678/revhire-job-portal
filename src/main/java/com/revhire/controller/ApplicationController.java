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

/**
 * ApplicationController handles all operations related to job applications.
 *
 * Responsibilities:
 * - Allow job seekers to apply for jobs.
 * - Allow employers to view applicants.
 * - Allow employers to add notes to applications.
 * - Allow job seekers to view their submitted applications.
 * - Handle resume and cover letter submission.
 */
@Controller
@RequestMapping("/applications")
public class ApplicationController {

    /**
     * Service responsible for handling application-related business logic.
     */
    @Autowired
    private ApplicationService applicationService;

    /**
     * Service responsible for retrieving job details.
     */
    @Autowired
    private JobService jobService;

    // ================= APPLY =================

    /**
     * Allows a job seeker to apply for a job.
     *
     * @param jobId ID of the job being applied for
     * @param jobSeekerId ID of the job seeker applying
     * @return redirect to the job seeker applications page
     */
    @PostMapping("/apply")
    public String applyJob(@RequestParam Long jobId,
            @RequestParam Long jobSeekerId) {

        System.out.println("Applying for Job ID: " + jobId + " by Seeker ID: " + jobSeekerId);
        applicationService.applyJob(jobId, jobSeekerId);

        return "redirect:/applications/jobseeker/" + jobSeekerId;
    }

    // ================= ADD NOTES =================

    /**
     * Allows an employer to add notes to a specific application.
     *
     * @param applicationId ID of the application
     * @param notes notes added by the employer
     * @param jobId optional job ID for redirection
     * @param referer previous page URL for redirection
     * @return redirect to the appropriate page after saving notes
     */
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

    /**
     * Displays all applications submitted by a specific job seeker.
     *
     * @param jobSeekerId ID of the job seeker
     * @param model Spring model used to pass data to the view
     * @return job seeker applications page
     */
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

    /**
     * Displays all applicants for a specific job.
     *
     * @param jobId ID of the job
     * @param model Spring model used to pass data to the view
     * @return employer applicants page
     */
    @GetMapping("/job/{jobId}")
    public String viewApplicationsByJob(
            @PathVariable Long jobId,
            Model model) {

        model.addAttribute("applications",
                applicationService.getApplicationsByJob(jobId));

        model.addAttribute("jobId", jobId);

        return "employer/applicants";
    }

    /**
     * Displays the job application page for a specific job.
     *
     * @param jobId ID of the job
     * @param model Spring model used to pass job details to the view
     * @return job application page
     */
    @GetMapping("/apply/{jobId}")
    public String showApplyPage(@PathVariable Long jobId, Model model) {

        Job job = jobService.getJobById(jobId);

        model.addAttribute("job", job);

        return "jobseeker/apply-job";
    }

    /**
     * Handles submission of a job application including resume and cover letter.
     *
     * @param jobId ID of the job
     * @param resume uploaded resume file
     * @param coverLetter optional cover letter text
     * @param principal authenticated user information
     * @param ra redirect attributes used to pass messages
     * @return redirect to job seeker dashboard after submission
     */
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