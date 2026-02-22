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
public class JobSeekerController {

    private final ResumeService resumeService;
    private final JobSeekerService jobSeekerService;
    private final EducationService educationService;
    private final UserService userService;

    public JobSeekerController(
            ResumeService resumeService,
            JobSeekerService jobSeekerService,
            EducationService educationService,
            UserService userService) {

        this.resumeService = resumeService;
        this.jobSeekerService = jobSeekerService;
        this.educationService = educationService;
        this.userService = userService;
    }

    // ✅ Dashboard
    @GetMapping("/jobseeker/dashboard")
    public String dashboard(Model model, Authentication auth, HttpSession session) {

        User user = userService.findByEmail(auth.getName());

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFullName());

        Long userId = user.getUserId();

        model.addAttribute("user", user);
        model.addAttribute("savedCount", jobSeekerService.getSavedJobsList(userId).size());
        model.addAttribute("applicationCount", jobSeekerService.getApplicationsList(userId).size());

        if (user instanceof JobSeeker js) {
            model.addAttribute("completion", js.calculateProfileCompletion());
        }

        return "jobseeker/dashboard";
    }

    // ✅ Search Jobs Page
    @GetMapping("/jobseeker/search-jobs")
    public String searchJobs(Model model, HttpSession session) {

        validateSession(session);

        model.addAttribute("jobs", jobSeekerService.getAllJobs());
        return "jobseeker/search_jobs";
    }

    // ✅ Saved Jobs Page
    @GetMapping("/jobseeker/saved-jobs")
    public String savedJobs(Model model, HttpSession session) {

        Long userId = validateSession(session);

        model.addAttribute("savedJobs", jobSeekerService.getSavedJobsList(userId));

        return "jobseeker/saved_jobs";
    }

    // ✅ Applications Page
    @GetMapping("/jobseeker/applications")
    public String applications(Model model, HttpSession session) {

        Long userId = validateSession(session);

        model.addAttribute("applications", jobSeekerService.getApplicationsList(userId));

        return "jobseeker/applications";
    }

    // ✅ Job Details
    @GetMapping("/jobseeker/job/{id}")
    public String jobDetails(@PathVariable Long id, Model model, HttpSession session) {

        validateSession(session);

        model.addAttribute("job", jobSeekerService.getJobById(id));

        return "jobseeker/job_details";
    }

    // ✅ Resume Page
    @GetMapping("/jobseeker/resume")
    public String resumePage(HttpSession session) {

        validateSession(session);

        return "jobseeker/resume";
    }

    // ✅ Profile Page
    @GetMapping("/jobseeker/profile")
    public String profile(Model model, HttpSession session) {

        Long userId = validateSession(session);

        model.addAttribute("user", userService.getUserById(userId));

        return "jobseeker/profile";
    }

    // ✅ Upload Resume
    @PostMapping("/uploadResume")
    public String uploadResume(@RequestParam MultipartFile file, HttpSession session) {

        validateSession(session);

        resumeService.uploadResume(file);
        return "redirect:/jobseeker/resume?success";
    }

    // ✅ Save Job API
    @PostMapping("/saveJob/{jobId}")
    @ResponseBody
    public ResponseEntity<?> saveJob(@PathVariable Long jobId, HttpSession session) {

        Long userId = validateSession(session);

        return jobSeekerService.saveJob(userId, jobId);
    }

    // ✅ ✅ ✅ SESSION VALIDATION (Reusable ⭐)
    private Long validateSession(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException("Session expired. Please login again.");
        }

        return userId;
    }
}