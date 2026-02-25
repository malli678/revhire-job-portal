package com.revhire.controller;

import com.revhire.dto.ResumeDto;
import com.revhire.exception.UnauthorizedException;
import com.revhire.model.*;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.JobRepository;
import com.revhire.service.*;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/jobseeker")
public class JobSeekerController {

    private final ResumeService resumeService;
    private final JobSeekerService jobSeekerService;
    private final EducationService educationService;
    private final UserService userService;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public JobSeekerController(ResumeService resumeService,
                               JobSeekerService jobSeekerService,
                               EducationService educationService,
                               UserService userService,
                               ApplicationRepository applicationRepository,
                               JobRepository jobRepository) {

        this.resumeService = resumeService;
        this.jobSeekerService = jobSeekerService;
        this.educationService = educationService;
        this.userService = userService;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth, HttpSession session) {

        User user = userService.findByEmail(auth.getName());

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());

        Long userId = user.getUserId();

        model.addAttribute("user", user);
        model.addAttribute("savedCount", jobSeekerService.getSavedJobsList(userId).size());
        model.addAttribute("applicationCount", jobSeekerService.getApplicationsList(userId).size());

        if (user instanceof JobSeeker js) {
            model.addAttribute("completion", js.calculateProfileCompletion());
        }

        return "jobseeker/dashboard";
    }

    // =========================
    // SEARCH JOBS PAGE
    // =========================
    @GetMapping("/search-jobs")
    public String searchJobs(Model model, HttpSession session) {

        validateSession(session);

        model.addAttribute("jobs", jobSeekerService.getAllJobs());
        model.addAttribute("role", "JOBSEEKER");

        return "jobseeker/search-jobs";
    }

    // =========================
    // SAVED JOBS PAGE
    // =========================
    @GetMapping("/saved-jobs")
    public String savedJobs(Model model, HttpSession session) {

        Long userId = validateSession(session);
        model.addAttribute("savedJobs", jobSeekerService.getSavedJobsList(userId));

        return "jobseeker/saved-jobs";
    }

    // =========================
    // APPLY JOB
    // =========================
    @PostMapping("/applyJob/{jobId}")
    @ResponseBody
    public ResponseEntity<String> applyJob(@PathVariable Long jobId,
                                           Authentication authentication) {

        try {

            String email = authentication.getName();

            User user = userService.findByEmail(email);

            if (user == null)
                throw new RuntimeException("User not found");

            return jobSeekerService.applyJob(user.getUserId(), jobId);

        } 
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // =========================
    // WITHDRAW APPLICATION 
    // =========================
    @PostMapping("/withdraw/{applicationId}")
    public String withdrawApplication(@PathVariable Long applicationId,
                                      @RequestParam String notes,
                                      HttpSession session) {

        validateSession(session);
        jobSeekerService.withdrawApplication(applicationId, notes);

        return "redirect:/jobseeker/applications";
    }

    // =========================
    // APPLICATIONS PAGE
    // =========================
    @GetMapping("/applications")
    public String viewApplications(Model model,
                                   HttpSession session) {

        Long userId = validateSession(session);

        model.addAttribute("applications",
                jobSeekerService.getApplicationsList(userId));

        return "jobseeker/applications";
    }

    // =========================
    // JOB DETAILS
    // =========================
    @GetMapping("/job/{id}")
    public String jobDetails(@PathVariable Long id, Model model, HttpSession session) {

        validateSession(session);
        model.addAttribute("job", jobSeekerService.getJobById(id));

        return "jobseeker/job-details"; // use consistent template name
    }

    // =========================
    // RESUME PAGE
    // =========================
    @GetMapping("/resume")
    public String resumePage(Model model, Principal principal, HttpSession session) {

        validateSession(session);

        User user = userService.findByEmail(principal.getName());
        JobSeeker js = (JobSeeker) user;

        ResumeDto preview = new ResumeDto();
        preview.setObjective(js.getObjective());
        preview.setDegree(js.getDegree());
        preview.setYear(js.getYear());

        if(js.getSkills() != null) {
            preview.setSkills(String.join(",", js.getSkills()));
        }

        model.addAttribute("resumeForm", new ResumeDto());   // EMPTY ⭐⭐⭐
        model.addAttribute("resumePreview", preview);        // SAVED ⭐⭐⭐

        return "jobseeker/resume";
    }

    // =========================
    // PROFILE PAGE
    // =========================
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {

        Long userId = validateSession(session);
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);

        if (user instanceof JobSeeker js) {
            model.addAttribute("completion", js.calculateProfileCompletion());
        }

        return "jobseeker/profile";
    }

    // =========================
    // UPLOAD RESUME
    // =========================
    @PostMapping("/uploadResume")
    public String uploadResume(@RequestParam MultipartFile file,
                               Principal principal,
                               HttpSession session) {

        validateSession(session);

        resumeService.uploadResume(file, principal.getName());  // Must match service method

        return "redirect:/jobseeker/resume?success";
    }
    // =========================
    // SAVE RESUME DETAILS
    // =========================
    @PostMapping("/saveResumeDetails")
    public String saveResumeDetails(ResumeDto dto,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {

        resumeService.save(dto, principal.getName());

        redirectAttributes.addFlashAttribute("successMsg", "Resume details saved successfully ✅");

        return "redirect:/jobseeker/resume";
    }

    // =========================
    // SAVE JOB
    // =========================
    @PostMapping("/saveJob/{jobId}")
    @ResponseBody
    public ResponseEntity<?> saveJob(@PathVariable Long jobId, HttpSession session) {

        Long userId = validateSession(session);
        return jobSeekerService.saveJob(userId, jobId);
    }

    // =========================
    // SESSION VALIDATION
    // =========================
    private Long validateSession(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException("Session expired. Please login again.");
        }
        return userId;
    }
}