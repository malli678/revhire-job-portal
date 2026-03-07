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
 * Controller responsible for handling Job Alert related operations.
 *
 * Job alerts allow job seekers to receive notifications when
 * new jobs matching their preferences are posted.
 *
 * This controller provides endpoints for:
 * - Viewing job alerts
 * - Creating new job alerts
 * - Activating or deactivating alerts
 * - Deleting alerts
 *
 * The controller communicates with:
 * - JobAlertService → Business logic for alerts
 * - JobSeekerService → Fetch logged-in job seeker information
 */
@Controller
@RequestMapping("/jobseeker/alerts")
public class JobAlertController {

    private final JobAlertService jobAlertService;
    private final JobSeekerService jobSeekerService;

    /**
     * Constructor injection for required services.
     *
     * @param jobAlertService service responsible for job alert logic
     * @param jobSeekerService service responsible for job seeker operations
     */
    public JobAlertController(JobAlertService jobAlertService,
                              JobSeekerService jobSeekerService) {
        this.jobAlertService = jobAlertService;
        this.jobSeekerService = jobSeekerService;
    }

    /**
     * Displays all job alerts created by the logged-in job seeker.
     *
     * Steps:
     * 1. Get logged-in user's email from Spring Security authentication
     * 2. Retrieve JobSeeker entity
     * 3. Fetch job alerts associated with the user
     * 4. Add alerts to the model
     *
     * @param model Spring model to pass attributes to Thymeleaf
     * @param authentication authenticated user session
     * @return job alerts view page
     */
    @GetMapping
    public String viewAlerts(Model model, Authentication authentication) {
        try {

            // Get logged in user email
            String email = authentication.getName();
            System.out.println("Logged in user email: " + email);

            // Fetch JobSeeker entity
            JobSeeker jobSeeker = jobSeekerService.getJobSeekerByEmail(email);
            System.out.println("JobSeeker ID: " + jobSeeker.getUserId());

            // Fetch job alerts
            List<JobAlert> alerts =
                    jobAlertService.getUserAlerts(jobSeeker.getUserId());

            System.out.println("Alerts found: " + alerts.size());

            model.addAttribute("alerts", alerts);

            return "jobseeker/job-alerts";

        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute(
                    "error",
                    "Error loading alerts: " + e.getMessage()
            );

            return "jobseeker/job-alerts";
        }
    }

    /**
     * Displays the job alert creation form.
     *
     * @param model Spring model
     * @return create alert form page
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {

        model.addAttribute("alert", new JobAlert());

        return "jobseeker/create-alert";
    }

    /**
     * Handles creation of a new job alert.
     *
     * Steps:
     * 1. Get logged-in job seeker
     * 2. Collect alert details from form
     * 3. Call JobAlertService to create alert
     * 4. Redirect to alerts page
     *
     * @param alert alert form data
     * @param authentication logged-in user
     * @param redirectAttributes flash messages
     * @return redirect to alerts page
     */
    @PostMapping("/create")
    public String createAlert(@ModelAttribute JobAlert alert,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        try {

            JobSeeker jobSeeker =
                    jobSeekerService.getJobSeekerByEmail(authentication.getName());

            jobAlertService.createAlert(
                    jobSeeker,
                    alert.getAlertName(),
                    alert.getKeywords(),
                    alert.getLocation(),
                    alert.getJobType(),
                    alert.getMinSalary(),
                    alert.getFrequency()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Job alert created successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to create alert: " + e.getMessage()
            );
        }

        return "redirect:/jobseeker/alerts";
    }

    /**
     * Activates or deactivates a job alert.
     *
     * Example:
     * - Turn ON alert
     * - Turn OFF alert
     *
     * @param id alert ID
     * @param active true = activate, false = deactivate
     * @param redirectAttributes flash messages
     * @return redirect to alerts page
     */
    @PostMapping("/toggle/{id}")
    public String toggleAlert(@PathVariable Long id,
                              @RequestParam boolean active,
                              RedirectAttributes redirectAttributes) {

        try {

            jobAlertService.toggleAlert(id, active);

            redirectAttributes.addFlashAttribute(
                    "success",
                    active ? "Alert activated" : "Alert deactivated"
            );

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to update alert"
            );
        }

        return "redirect:/jobseeker/alerts";
    }

    /**
     * Deletes a job alert created by the user.
     *
     * @param id alert ID
     * @param redirectAttributes flash messages
     * @return redirect to alerts page
     */
    @PostMapping("/delete/{id}")
    public String deleteAlert(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {

        try {

            jobAlertService.deleteAlert(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Alert deleted successfully"
            );

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to delete alert"
            );
        }

        return "redirect:/jobseeker/alerts";
    }
}