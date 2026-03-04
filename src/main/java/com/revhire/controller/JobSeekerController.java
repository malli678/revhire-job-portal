package com.revhire.controller;

import com.revhire.dto.ResumeDto;
import com.revhire.exception.UnauthorizedException;
import com.revhire.model.*;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.JobRepository;
import com.revhire.repository.JobSeekerRepository;
import com.revhire.service.*;
import java.util.List;
import java.io.File;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.nio.file.*;
import java.nio.file.Paths;
import java.security.Principal;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/jobseeker")
public class JobSeekerController {

    private final FileStorageService fileStorageService;
    private final JobSeekerRepository jobSeekerRepository;
    private final JobService jobService;
    private final RecommendationService recommendationService;
    private final ApplicationService applicationService;
    private final ResumeParserService resumeParserService;
    private final ResumeService resumeService;
    private final JobSeekerService jobSeekerService;
    private final EducationService educationService;
    private final UserService userService;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public JobSeekerController(FileStorageService fileStorageService,
            JobSeekerRepository jobSeekerRepository,
            JobService jobService,
            RecommendationService recommendationService,
            ApplicationService applicationService,
            ResumeParserService resumeParserService,
            ResumeService resumeService,
            JobSeekerService jobSeekerService,
            EducationService educationService,
            UserService userService,
            ApplicationRepository applicationRepository,
            JobRepository jobRepository) {
        this.fileStorageService = fileStorageService;
        this.jobSeekerRepository = jobSeekerRepository;
        this.jobService = jobService;
        this.recommendationService = recommendationService;
        this.applicationService = applicationService;
        this.resumeParserService = resumeParserService;
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

    @PostMapping("/applyJob/{jobId}")
    public String applyJob(@PathVariable Long jobId,
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            @RequestParam(required = false) String coverLetter,
            Authentication authentication,
            RedirectAttributes ra) {

        try {
            applicationService.apply(jobId, resume, coverLetter, authentication.getName());
            ra.addFlashAttribute("successMsg", "Application submitted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/jobseeker/job/" + jobId;
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

        boolean alreadyApplied = applicationRepository.findByJobAndJobSeeker(job, js).isPresent();

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

        if (js.getSkillEntities() != null && !js.getSkillEntities().isEmpty()) {
            preview.setSkills(
                    js.getSkillEntities()
                            .stream()
                            .map(Skill::getName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));
        } else if (js.getSkills() != null && !js.getSkills().isEmpty()) {
            preview.setSkills(
                    js.getSkills()
                            .stream()
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));
        }

        ResumeDto formDto = new ResumeDto();
        formDto.setObjective(js.getObjective());
        formDto.setDegree(js.getDegree());
        formDto.setYear(js.getYear());
        formDto.setSkills(preview.getSkills());

        model.addAttribute("resumeForm", formDto); // PREFILLED WITH PARSED DATA ⭐⭐⭐
        model.addAttribute("resumePreview", preview); // SAVED ⭐⭐⭐
        model.addAttribute("resumeFile", js.getResumeFile());
        return "jobseeker/resume";
    }

    @GetMapping("/downloadResume")
    @ResponseBody
    public ResponseEntity<Resource> downloadResume(Principal principal) {
        try {
            JobSeeker js = jobSeekerService.getJobSeekerByEmail(principal.getName());

            String resumeFile = js.getResumeFile();
            if (resumeFile == null || resumeFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Path path = Paths.get("uploads").resolve(resumeFile).normalize();
            File file = path.toFile();
            if (file == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Resource resource = new FileSystemResource(file);

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String contentType = "application/octet-stream";
            try {
                contentType = Files.probeContentType(path);
                if (contentType == null) {
                    String lowerName = resumeFile.toLowerCase();
                    if (lowerName.endsWith(".pdf"))
                        contentType = "application/pdf";
                    else if (lowerName.endsWith(".doc"))
                        contentType = "application/msword";
                    else if (lowerName.endsWith(".docx"))
                        contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                }
            } catch (Exception e) {
                // Ignore probing errors
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Jobseeker Download Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

        // Force reload from database to get latest data
        JobSeeker freshJs = jobSeekerRepository.findById(js.getUserId())
                .orElseThrow(() -> new RuntimeException("JobSeeker not found"));

        model.addAttribute("user", freshJs);
        model.addAttribute("skills", freshJs.getSkillEntities());
        model.addAttribute("education", freshJs.getEducationEntities());
        model.addAttribute("certifications", freshJs.getCertificationEntities());
        model.addAttribute("completion", freshJs.calculateProfileCompletion());

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
            // Get logged-in jobseeker
            JobSeeker js = jobSeekerService.getJobSeekerByEmail(principal.getName());

            // Save file and parse resume
            String fileName = fileStorageService.storeFile(file);
            js.setResumeFile(fileName);
            jobSeekerRepository.save(js);

            // Parse resume asynchronously
            resumeParserService.parseAndUpdateResume(js.getUserId(), file);

            ra.addFlashAttribute("successMsg", "Resume uploaded successfully. Parsing in background...");

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

        redirectAttributes.addFlashAttribute("successMsg", "Resume details saved successfully");

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

    @GetMapping("/recommendations")
    public String viewRecommendations(Model model, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        JobSeeker jobSeeker = (JobSeeker) user;

        List<Job> recommendedJobs = recommendationService.getRecommendedJobs(
                jobSeeker.getUserId(), 10);

        model.addAttribute("recommendedJobs", recommendedJobs);

        return "jobseeker/recommendations";
    }

    @PostMapping("/profile/update/personal")
    public String updatePersonalInfo(Authentication auth,
            @RequestParam String phoneNumber,
            @RequestParam String location,
            RedirectAttributes ra) {
        try {
            JobSeeker js = jobSeekerService.getJobSeekerByEmail(auth.getName());
            js.setPhoneNumber(phoneNumber);
            js.setLocation(location);
            jobSeekerRepository.save(js);
            ra.addFlashAttribute("success", "Personal information updated");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jobseeker/profile";
    }

   
    // PROFILE PICTURE MANAGEMENT
    
    @PostMapping("/uploadProfilePicture")
    public String uploadProfilePicture(@RequestParam MultipartFile file,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            JobSeeker js = jobSeekerService.getJobSeekerByEmail(auth.getName());
            String fileName = fileStorageService.storeFile(file); // Assume standard image storage
            js.setProfilePicture(fileName);
            jobSeekerRepository.save(js);
            ra.addFlashAttribute("success", "Profile picture updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error uploading picture: " + e.getMessage());
        }
        return "redirect:/jobseeker/profile";
    }

    @PostMapping("/removeProfilePicture")
    public String removeProfilePicture(Authentication auth, RedirectAttributes ra) {
        try {
            JobSeeker js = jobSeekerService.getJobSeekerByEmail(auth.getName());
            js.setProfilePicture(null);
            jobSeekerRepository.save(js);
            ra.addFlashAttribute("success", "Profile picture removed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error removing picture.");
        }
        return "redirect:/jobseeker/profile";
    }

    @PostMapping("/profile/update/employment")
    public String updateEmploymentInfo(Authentication auth,
            @RequestParam(required = false) String currentEmploymentStatus,
            @RequestParam(required = false) String currentCompany,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) Integer totalExperienceYears,
            RedirectAttributes ra) {
        try {
            JobSeeker js = jobSeekerService.getJobSeekerByEmail(auth.getName());
            js.setCurrentEmploymentStatus(currentEmploymentStatus);
            js.setCurrentCompany(currentCompany);
            js.setDesignation(designation);
            js.setTotalExperienceYears(totalExperienceYears);
            jobSeekerRepository.save(js);
            ra.addFlashAttribute("success", "Employment information updated");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jobseeker/profile";
    }
}