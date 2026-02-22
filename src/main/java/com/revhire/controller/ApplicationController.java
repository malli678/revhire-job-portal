package com.revhire.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.revhire.service.ApplicationService;
import com.revhire.model.Application;

import java.util.List;

import org.springframework.ui.Model;

//@RestController
@Controller

@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;


    
    @PostMapping("/apply")
    public String applyJob(@RequestParam Long jobId,
                           @RequestParam Long jobSeekerId) {

        applicationService.applyJob(jobId, jobSeekerId);

        return "redirect:/applications/my-applications?jobSeekerId=" + jobSeekerId;
    }

    // WITHDRAW
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam Long applicationId) {

        applicationService.withdrawApplication(applicationId);
        return "Application withdrawn successfully";
    }

    // UPDATE STATUS
    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long applicationId,
                               @RequestParam Application.ApplicationStatus status) {

        applicationService.updateApplicationStatus(applicationId, status);
        return "Status updated successfully";
    }
    
    //BULK UPDATE
    @PostMapping("/bulk-update")
    public String bulkUpdate(@RequestParam List<Long> applicationIds,
                             @RequestParam Application.ApplicationStatus status) {

        applicationService.bulkUpdateStatus(applicationIds, status);
        return "Bulk status updated";
    }
    
    //ADDING NOTES
    @PostMapping("/add-notes")
    public String addNotes(@RequestParam Long applicationId,
                           @RequestParam String notes) {

        applicationService.addNotes(applicationId, notes);
        return "Notes added";
    }


    // VIEW APPLICATIONS BY JOB SEEKER

    
    
    @GetMapping("/jobseeker/{jobSeekerId}")
    public String viewApplicationsByJobSeeker(
            @PathVariable Long jobSeekerId,
            Model model) {

        model.addAttribute("applications",
                applicationService.getApplicationsByJobSeeker(jobSeekerId));

        model.addAttribute("jobSeekerId", jobSeekerId);

        return "jobseeker/applications";
    }

    // VIEW APPLICATIONS BY JOB

    
    
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