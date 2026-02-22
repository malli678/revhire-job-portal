package com.revhire.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.revhire.exception.UnauthorizedException;
import com.revhire.model.Education;
import com.revhire.service.EducationService;
import com.revhire.service.JobSeekerService;
import com.revhire.service.ResumeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class JobSeekerController {

    private final ResumeService resumeService;
    private final JobSeekerService jobSeekerService;
    private final EducationService educationService;

    public JobSeekerController(
            ResumeService resumeService,
            JobSeekerService jobSeekerService,
            EducationService educationService) {

        this.resumeService = resumeService;
        this.jobSeekerService = jobSeekerService;
        this.educationService = educationService;
    }

    // ✅ Upload Resume
    @PostMapping("/uploadResume")
    public String uploadResume(@RequestParam("file") MultipartFile file) {

        resumeService.uploadResume(file);

        return "redirect:/jobseeker/resume?success";
    }

    // ✅ Save Job
    @PostMapping("/saveJob/{jobId}")
    @ResponseBody
    public ResponseEntity<?> saveJob(@PathVariable Long jobId, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException("Please login first");
        }

        return jobSeekerService.saveJob(userId, jobId);
    }

    // ✅ View Saved Jobs
    @GetMapping("/saved-jobs")
    @ResponseBody
    public ResponseEntity<?> viewSavedJobs(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException("Please login first");
        }

        return jobSeekerService.getSavedJobs(userId);
    }

    // ✅ Add Education
    @PostMapping("/education/add")
    @ResponseBody
    public ResponseEntity<?> addEducation(@ModelAttribute Education education,
                                          HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException("Please login first");
        }

        return educationService.addEducation(userId, education);
    }

    // ✅ View Education
    @GetMapping("/education")
    @ResponseBody
    public ResponseEntity<?> viewEducation(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException("Please login first");
        }

        return educationService.getEducation(userId);
    }

    // ✅ Delete Education
    @DeleteMapping("/education/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteEducation(@PathVariable Long id) {

        return educationService.deleteEducation(id);
    }
}