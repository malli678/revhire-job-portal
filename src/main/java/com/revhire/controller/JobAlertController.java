package com.revhire.controller;

import com.revhire.model.JobAlert;
import com.revhire.model.JobSeeker;
import com.revhire.service.JobAlertService;
import com.revhire.service.JobSeekerService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/jobseeker/alerts")
public class JobAlertController {
    
    private final JobAlertService jobAlertService;
    private final JobSeekerService jobSeekerService;
    
    public JobAlertController(JobAlertService jobAlertService,
                              JobSeekerService jobSeekerService) {
        this.jobAlertService = jobAlertService;
        this.jobSeekerService = jobSeekerService;
    }
    
    @GetMapping
    public String viewAlerts(Model model, Authentication authentication) {
        try {
            // Get logged in user
            String email = authentication.getName();
            System.out.println("Logged in user email: " + email); // Debug log
            
            JobSeeker jobSeeker = jobSeekerService.getJobSeekerByEmail(email);
            System.out.println("JobSeeker ID: " + jobSeeker.getUserId()); // Debug log
            
            // Get alerts
            List<JobAlert> alerts = jobAlertService.getUserAlerts(jobSeeker.getUserId());
            System.out.println("Alerts found: " + alerts.size()); // Debug log
            
            // Add to model
            model.addAttribute("alerts", alerts);
            
            return "jobseeker/job-alerts"; // Make sure this path is correct
            
        } catch (Exception e) {
            e.printStackTrace(); // Print full error
            model.addAttribute("error", "Error loading alerts: " + e.getMessage());
            return "jobseeker/job-alerts";
        }
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("alert", new JobAlert());
        return "jobseeker/create-alert";
    }
    
    @PostMapping("/create")
    public String createAlert(@ModelAttribute JobAlert alert,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            JobSeeker jobSeeker = jobSeekerService.getJobSeekerByEmail(authentication.getName());
            
            jobAlertService.createAlert(
                jobSeeker,
                alert.getAlertName(),
                alert.getKeywords(),
                alert.getLocation(),
                alert.getJobType(),
                alert.getMinSalary(),
                alert.getFrequency()
            );
            
            redirectAttributes.addFlashAttribute("success", "Job alert created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to create alert: " + e.getMessage());
        }
        
        return "redirect:/jobseeker/alerts";
    }
    
    @PostMapping("/toggle/{id}")
    public String toggleAlert(@PathVariable Long id,
                              @RequestParam boolean active,
                              RedirectAttributes redirectAttributes) {
        try {
            jobAlertService.toggleAlert(id, active);
            redirectAttributes.addFlashAttribute("success", 
                active ? "Alert activated" : "Alert deactivated");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update alert");
        }
        
        return "redirect:/jobseeker/alerts";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteAlert(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            jobAlertService.deleteAlert(id);
            redirectAttributes.addFlashAttribute("success", "Alert deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete alert");
        }
        
        return "redirect:/jobseeker/alerts";
    }
}