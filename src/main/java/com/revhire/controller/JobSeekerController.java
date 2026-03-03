package com.revhire.controller;

import com.revhire.dto.ResumeDto;
import com.revhire.exception.UnauthorizedException;
import com.revhire.model.*;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.JobRepository;
import com.revhire.repository.JobSeekerRepository;
import com.revhire.service.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/jobseeker")
public class JobSeekerController {

    // =========================
    // DEPENDENCIES
    // =========================

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JobSeekerRepository jobSeekerRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ResumeParserService resumeParserService;

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
    // APPLY JOB (RESUME + COVER LETTER KEPT)
    // =========================

    @PostMapping("/applyJob/{jobId}")
    public String applyJob(@PathVariable Long jobId,
                           @RequestParam("resume") MultipartFile resume,
                           @RequestParam(required = false) String coverLetter,
                           Authentication authentication,
                           RedirectAttributes ra) {

        try {

            JobSeeker js =
                    jobSeekerService.getJobSeekerByEmail(authentication.getName());

            jobSeekerService.applyJobWithResume(
                    js.getUserId(),
                    jobId,
                    resume,
                    coverLetter
            );

            ra.addFlashAttribute("successMsg",
                    "Application submitted successfully!");

        } catch (Exception e) {

            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }

        return "redirect:/jobseeker/job/" + jobId;
    }

    // =========================
    // JOB DETAILS
    // =========================

    @GetMapping("/job/{jobId}")
    public String jobDetails(@PathVariable Long jobId,
                             Model model,
                             Principal principal) {

        Job job = jobService.getJobById(jobId);
        JobSeeker js = jobSeekerService.getJobSeekerByEmail(principal.getName());

        boolean alreadyApplied =
                applicationRepository.findByJobAndJobSeeker(job, js).isPresent();

        model.addAttribute("job", job);
        model.addAttribute("alreadyApplied", alreadyApplied);

        return "jobseeker/job-details";
    }

    // =========================
    // DOWNLOAD RESUME
    // =========================

    @GetMapping("/downloadResume")
    @ResponseBody
    public ResponseEntity<Resource> downloadResume(Principal principal) {

        JobSeeker js = jobSeekerService.getJobSeekerByEmail(principal.getName());

        if (js.getResumeFile() == null) {
            throw new RuntimeException("No resume uploaded");
        }

        Path path = Paths.get("uploads").resolve(js.getResumeFile());
        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + js.getResumeFile() + "\"")
                .body(resource);
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
 // =========================
 // SEARCH JOBS PAGE
 // =========================
 @GetMapping("/search-jobs")
 public String searchJobs(Model model, HttpSession session) {

     validateSession(session);

     model.addAttribute("jobs", jobSeekerService.getAllJobs());
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
 // APPLICATIONS PAGE
 // =========================
 @GetMapping("/applications")
 public String viewApplications(Model model, HttpSession session) {

     Long userId = validateSession(session);
     model.addAttribute("applications",
             jobSeekerService.getApplicationsList(userId));

     return "jobseeker/applications";
 }

 // =========================
 // PROFILE PAGE
 // =========================
 @GetMapping("/profile")
 public String profile(Model model, HttpSession session, Authentication auth) {

     Long userId = validateSession(session);
     User user = userService.getUserById(userId);

     if (!(user instanceof JobSeeker js)) {
         throw new RuntimeException("Invalid user role");
     }

     model.addAttribute("user", js);
     model.addAttribute("skills", js.getSkillEntities());
     model.addAttribute("education", js.getEducationEntities());
     model.addAttribute("certifications", js.getCertificationEntities());
     model.addAttribute("completion", js.calculateProfileCompletion());

     return "jobseeker/profile";
 }

 // =========================
 // RESUME PAGE
 // =========================
 @GetMapping("/resume")
 public String resumePage(Model model, Principal principal, HttpSession session) {

     validateSession(session);

     JobSeeker js = jobSeekerService.getJobSeekerByEmail(principal.getName());

     model.addAttribute("resumeFile", js.getResumeFile());
     return "jobseeker/resume";
 }

 // =========================
 // RECOMMENDATIONS PAGE
 // =========================
 @GetMapping("/recommendations")
 public String viewRecommendations(Model model, Authentication authentication) {

     User user = userService.findByEmail(authentication.getName());
     JobSeeker jobSeeker = (JobSeeker) user;

     List<Job> recommendedJobs =
             recommendationService.getRecommendedJobs(jobSeeker.getUserId(), 10);

     model.addAttribute("recommendedJobs", recommendedJobs);

     return "jobseeker/recommendations";
 }

 

}