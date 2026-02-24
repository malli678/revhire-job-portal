package com.revhire.controller;

import com.revhire.exception.UnauthorizedException;
import com.revhire.model.*;
import com.revhire.service.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/jobseeker")
public class JobSeekerController {

    private final ResumeService resumeService;
    private final JobSeekerService jobSeekerService;
    private final EducationService educationService;
    private final UserService userService;

    public JobSeekerController(ResumeService resumeService,
                               JobSeekerService jobSeekerService,
                               EducationService educationService,
                               UserService userService) {

        this.resumeService = resumeService;
        this.jobSeekerService = jobSeekerService;
        this.educationService = educationService;
        this.userService = userService;
    }

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            Authentication auth,
                            HttpSession session) {

        User user = userService.findByEmail(auth.getName());

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());

        Long userId = user.getUserId();

        model.addAttribute("user", user);
        model.addAttribute("savedCount",
                jobSeekerService.getSavedJobsList(userId).size());
        model.addAttribute("applicationCount",
                jobSeekerService.getApplicationsList(userId).size());

        if (user instanceof JobSeeker js) {
            model.addAttribute("completion",
                    js.calculateProfileCompletion());
        }

        return "jobseeker/dashboard";
    }

    // =========================
    // SEARCH JOBS PAGE
    // =========================
    @GetMapping("/search-jobs")
    public String searchJobs(Model model,
                             HttpSession session) {

        validateSession(session);

        model.addAttribute("jobs",
                jobSeekerService.getAllJobs());
        model.addAttribute("role", "JOBSEEKER");

        return "jobseeker/search-jobs";
    }

    // =========================
    // SAVED JOBS PAGE
    // =========================
    @GetMapping("/saved-jobs")
    public String savedJobs(Model model,
                            HttpSession session) {

        Long userId = validateSession(session);

        model.addAttribute("savedJobs",
                jobSeekerService.getSavedJobsList(userId));

        return "jobseeker/saved-jobs";
    }

    // =========================
    // APPLY JOB
    // =========================
    @PostMapping("/applyJob/{jobId}")
    @ResponseBody
    public ResponseEntity<?> applyJob(@PathVariable Long jobId,
                                      HttpSession session) {

       // Long userId = (Long) session.getAttribute("userId");
    	Long userId = validateSession(session);
    	

        System.out.println("SESSION userId = " + userId);

        return jobSeekerService.applyJob(userId, jobId);
    }

    // =========================
    // APPLICATIONS PAGE
    // =========================
    @GetMapping("/applications")
    public String applications(Model model,
                               HttpSession session) {

        Long userId = validateSession(session);

        model.addAttribute("applications",
                jobSeekerService.getApplicationsList(userId));

        return "jobseeker/applications";
    }

    // =========================
    // JOB DETAILS PAGE
    // =========================
    @GetMapping("/job/{id}")
    public String jobDetails(@PathVariable Long id,
                             Model model,
                             HttpSession session) {

        validateSession(session);

        model.addAttribute("job",
                jobSeekerService.getJobById(id));

        return "jobseeker/job-details";
    }

    // =========================
    // RESUME PAGE
    // =========================
    @GetMapping("/resume")
    public String resumePage(HttpSession session) {
        validateSession(session);
        return "jobseeker/resume";
    }

    // =========================
    // PROFILE PAGE
    // =========================
    @GetMapping("/profile")
    public String profile(Model model,
                          HttpSession session) {

        Long userId = validateSession(session);

        model.addAttribute("user",
                userService.getUserById(userId));

        return "jobseeker/profile";
    }

    // =========================
    // UPLOAD RESUME
    // =========================
    @PostMapping("/uploadResume")
    public String uploadResume(@RequestParam MultipartFile file,
                               HttpSession session) {

        validateSession(session);
        resumeService.uploadResume(file);

        return "redirect:/jobseeker/resume?success";
    }

    // =========================
    // SAVE JOB
    // =========================
    @PostMapping("/saveJob/{jobId}")
    @ResponseBody
    public ResponseEntity<?> saveJob(@PathVariable Long jobId,
                                     HttpSession session) {

        Long userId = validateSession(session);
        return jobSeekerService.saveJob(userId, jobId);
    }

    // =========================
    // SESSION VALIDATION
    // =========================
    private Long validateSession(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException(
                    "Session expired. Please login again.");
        }

        return userId;
    }
    
    //withdraw
    @PostMapping("/withdraw/{applicationId}")
    public String withdrawApplication(@PathVariable Long applicationId,
                                      @RequestParam String notes,
                                      HttpSession session) {

        validateSession(session);

        jobSeekerService.withdrawApplication(applicationId, notes);

        return "redirect:/jobseeker/applications";
    }
}