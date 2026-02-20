package com.revhire.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.revhire.exception.UnauthorizedException;
import com.revhire.model.Education;
import com.revhire.service.EducationService;
import com.revhire.service.JobSeekerService;
import com.revhire.service.ResumeService;

import jakarta.servlet.http.HttpSession;

public class JobSeekerController {
	private ResumeService resumeservice;
	
	@PostMapping("/uploadResume")
	public String uploadResume(@RequestParam("file") MultipartFile file) {

	    resumeservice.uploadResume(file);

	    return "redirect:/jobseeker/resume?success";
	}
	
	private JobSeekerService jobseekerservice;
	@PostMapping("/saveJob/{jobId}")
	public ResponseEntity<?> saveJob(@PathVariable Long jobId, HttpSession session) {

	    Long userId = (Long) session.getAttribute("userId");

	    if (userId == null) {
	        throw new UnauthorizedException("Please login first");
	    }

	    return jobseekerservice.saveJob(userId, jobId);
	}
	
	
	@GetMapping("/saved-jobs")
	public ResponseEntity<?> viewSavedJobs(HttpSession session) {

	    Long userId = (Long) session.getAttribute("userId");

	    if (userId == null) {
	        throw new UnauthorizedException("Please login first");
	    }

	    return jobseekerservice.getSavedJobs(userId);
	}
	
	private EducationService educationservice;
	
	@PostMapping("/education/add")
	public ResponseEntity<?> addEducation(@ModelAttribute Education education,
	                                      HttpSession session) {

	    Long userId = (Long) session.getAttribute("userId");

	    if (userId == null) {
	        throw new UnauthorizedException("Please login first");
	    }

	    return educationservice.addEducation(userId, education);
	}
	
	@GetMapping("/education")
	public ResponseEntity<?> viewEducation(HttpSession session) {

	    Long userId = (Long) session.getAttribute("userId");

	    if (userId == null) {
	        throw new UnauthorizedException("Please login first");
	    }

	    return educationservice.getEducation(userId);
	}

	
	@DeleteMapping("/education/delete/{id}")
	public ResponseEntity<?> deleteEducation(@PathVariable Long id) {

	    return educationservice.deleteEducation(id);
	}
}
