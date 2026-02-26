package com.revhire.controller;

import com.revhire.dto.ResumeDto;
import com.revhire.exception.UnauthorizedException;
import com.revhire.model.*;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.JobRepository;
import com.revhire.repository.JobSeekerRepository;
import com.revhire.service.*;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.nio.file.*;
import jakarta.servlet.http.HttpSession;

import java.nio.file.Paths;
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
	@Autowired
	private FileStorageService fileStorageService;
	@Autowired
	private JobSeekerRepository jobSeekerRepository;
	@Autowired
	private JobService jobService;

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
    // REMOVE SAVED JOB
    // =========================
    @PostMapping("/removeSaved/{jobId}")
    @ResponseBody
    public ResponseEntity<?> removeSavedJob(@PathVariable Long jobId,
                                            HttpSession session) {

        Long userId = validateSession(session);
        return jobSeekerService.removeSavedJob(userId, jobId);
    }

    // =========================
    // APPLY JOB
    // =========================
    @PostMapping("/applyJob/{jobId}")
    @ResponseBody
    public ResponseEntity<String> applyJob(@PathVariable Long jobId,
                                           Authentication authentication) {

        try {

            User user = userService.findByEmail(authentication.getName());

            return jobSeekerService.applyJob(user.getUserId(), jobId);

        } catch (RuntimeException e) {

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
    @GetMapping("/job/{jobId}")
    public String jobDetails(@PathVariable Long jobId,
                             Model model,
                             Principal principal) {

        Job job = jobService.getJobById(jobId);

        JobSeeker js = jobSeekerService.getJobSeekerByEmail(principal.getName());

        boolean alreadyApplied =
                applicationRepository.findByJobAndJobSeeker(job, js).isPresent();

        model.addAttribute("job", job);

        // ⭐⭐⭐ IMPORTANT
        model.addAttribute("alreadyApplied", alreadyApplied);

        return "jobseeker/job-details";
    }

    // =========================
    // RESUME PAGE
    // =========================
    @GetMapping("/resume")
    public String resumePage(Model model, Principal principal, HttpSession session) {

        validateSession(session);

        User user = userService.findByEmail(principal.getName());
        if (!(user instanceof JobSeeker js)) {
            throw new RuntimeException("Invalid user role");
        }

        ResumeDto preview = new ResumeDto();
        preview.setObjective(js.getObjective());
        preview.setDegree(js.getDegree());
        preview.setYear(js.getYear());

        if(js.getSkillEntities() != null) {

            preview.setSkills(
                js.getSkillEntities()
                  .stream()
                  .map(Skill::getName)
                  .reduce((a,b) -> a + "," + b)
                  .orElse("")
            );
        }

        model.addAttribute("resumeForm", new ResumeDto());   // EMPTY ⭐⭐⭐
        model.addAttribute("resumePreview", preview);        // SAVED ⭐⭐⭐
        model.addAttribute("resumeFile", js.getResumeFile());
        return "jobseeker/resume";
    }
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
    // PROFILE PAGE
    // =========================
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {

        Long userId = validateSession(session);
        User user = userService.getUserById(userId);

        if (!(user instanceof JobSeeker js)) {
            throw new RuntimeException("Invalid user role");
        }

        model.addAttribute("user", js);

        // ✅ REQUIRED ⭐⭐⭐
        model.addAttribute("skills", js.getSkillEntities());
        model.addAttribute("education", js.getEducationEntities());
        model.addAttribute("certifications", js.getCertificationEntities());

        model.addAttribute("completion", js.calculateProfileCompletion());

        return "jobseeker/profile";
    }
    @PostMapping("/profile/skill/add")
    public String addSkill(Authentication auth,
                           @RequestParam String skillName) {

        JobSeeker js = jobSeekerService.getJobSeekerByEmail(auth.getName());

        jobSeekerService.addSkill(js.getUserId(), skillName);

        return "redirect:/jobseeker/profile";
    }
    @PostMapping("/profile/skill/delete/{id}")
    public String deleteSkill(@PathVariable Long id) {

        jobSeekerService.deleteSkill(id);

        return "redirect:/jobseeker/profile";
    }
    @PostMapping("/profile/education/add")
    public String addEducation(Authentication auth,
                               @RequestParam String degree,
                               @RequestParam String institution) {

        JobSeeker js = jobSeekerService.getJobSeekerByEmail(auth.getName());

        jobSeekerService.addEducation(js.getUserId(), degree, institution);

        return "redirect:/jobseeker/profile";
    }
    @PostMapping("/profile/education/delete/{id}")
    public String deleteEducation(@PathVariable Long id) {

        jobSeekerService.deleteEducation(id);

        return "redirect:/jobseeker/profile";
    }
    @PostMapping("/profile/certification/add")
    public String addCertification(Authentication auth,
                                   @RequestParam String name,
                                   @RequestParam String issuer,
                                   @RequestParam String year) {

        JobSeeker js = jobSeekerService.getJobSeekerByEmail(auth.getName());

        jobSeekerService.addCertification(js.getUserId(), name, issuer, year);

        return "redirect:/jobseeker/profile";
    }
    @PostMapping("/profile/certification/delete/{id}")
    public String deleteCertification(@PathVariable Long id) {

        jobSeekerService.deleteCertification(id);

        return "redirect:/jobseeker/profile";
    }
    // =========================
    // UPLOAD RESUME
    // =========================
    @PostMapping("/uploadResume")
    public String uploadResume(@RequestParam MultipartFile file,
                               Principal principal,
                               RedirectAttributes ra,
                               HttpSession session) {

        validateSession(session);

        try {

            // ✅ Save file to uploads folder
            String fileName = fileStorageService.storeFile(file);

            // ✅ Get logged-in jobseeker
            JobSeeker js = jobSeekerService.getJobSeekerByEmail(principal.getName());

            // ✅ Save filename into DB
            js.setResumeFile(fileName);

            jobSeekerRepository.save(js);

            ra.addFlashAttribute("successMsg", "Resume uploaded successfully ✅");

        } catch (Exception e) {

            ra.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/jobseeker/resume";
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