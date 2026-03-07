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

/**
 * JobAlertController manages job alert related operations for job seekers.
 *
 * Responsibilities:
 * - Display job alerts created by the job seeker.
 * - Allow job seekers to create new job alerts.
 * - Enable or disable existing job alerts.
 * - Delete job alerts when they are no longer needed.
 */
@Controller
@RequestMapping("/jobseeker/alerts")
public class JobAlertController {
    
    /**
     * Service used to manage job alert related operations.
     */
    private final JobAlertService jobAlertService;

    /**
     * Service used to retrieve job seeker information.
     */
    private final JobSeekerService jobSeekerService;
    
    /**
     * Constructor used for dependency injection.
     *
     * @param jobAlertService service responsible for job alert management
     * @param jobSeekerService service used to retrieve job seeker details
     */
    public JobAlertController(JobAlertService jobAlertService,
                              JobSeekerService jobSeekerService) {
        this.jobAlertService = jobAlertService;
        this.jobSeekerService = jobSeekerService;
    }

    /**
     * Displays all job alerts created by the logged-in job seeker.
     *
     * Steps performed:
     * - Retrieve authenticated user's email.
     * - Fetch job seeker details using the email.
     * - Retrieve job alerts associated with the job seeker.
     * - Add alerts to the model for view rendering.
     *
     * @param model Spring model used to pass data to the view
     * @param authentication Spring Security authentication object
     * @return job alerts page
     */
    @GetMapping
    public String viewAlerts(Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            System.out.println("Logged in user email: " + email);

            JobSeeker jobSeeker = jobSeekerService.getJobSeekerByEmail(email);
            System.out.println("JobSeeker ID: " + jobSeeker.getUserId());

            List<JobAlert> alerts = jobAlertService.getUserAlerts(jobSeeker.getUserId());
            System.out.println("Alerts found: " + alerts.size());

            model.addAttribute("alerts", alerts);

            return "jobseeker/job-alerts";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading alerts: " + e.getMessage());
            return "jobseeker/job-alerts";
        }
    }

    /**
     * Displays the form used to create a new job alert.
     *
     * @param model Spring model used to bind alert data
     * @return create job alert page
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("alert", new JobAlert());
        return "jobseeker/create-alert";
    }

    /**
     * Handles creation of a new job alert.
     *
     * Steps performed:
     * - Retrieve the logged-in job seeker.
     * - Pass alert details to the JobAlertService.
     * - Store success or error messages in redirect attributes.
     *
     * @param alert job alert details submitted by the user
     * @param authentication authenticated user information
     * @param redirectAttributes used to pass messages after redirect
     * @return redirect to job alerts page
     */
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

    /**
     * Enables or disables a job alert.
     *
     * @param id alert ID
     * @param active true if alert should be active, false otherwise
     * @param redirectAttributes used to pass status messages
     * @return redirect to job alerts page
     */
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

    /**
     * Deletes a job alert.
     *
     * @param id alert ID
     * @param redirectAttributes used to pass result messages
     * @return redirect to job alerts page
     */
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