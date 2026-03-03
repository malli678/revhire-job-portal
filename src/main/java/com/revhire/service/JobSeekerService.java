package com.revhire.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.revhire.exception.ResourceNotFoundException;
import com.revhire.exception.FileStorageException; // reuse for business validation if needed
import com.revhire.model.*;
import com.revhire.model.Application.ApplicationStatus;
import com.revhire.repository.*;

@Service
public class JobSeekerService {

	@Autowired
	private SavedJobRepository savedJobRepository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobSeekerRepository jobSeekerRepository;

	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private SkillRepository skillRepository;
	@Autowired
	private EducationRepository educationRepository;
	@Autowired
	private CertificationRepository certificationRepository;
	@Autowired
	private NotificationService notificationService;

	// =========================
	// SAVE JOB
	// =========================
	public ResponseEntity<?> saveJob(Long jobSeekerId, Long jobId) {

		JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
				.orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

		Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));

		// Check if already saved
		savedJobRepository.findByJobSeekerAndJob(jobSeeker, job).ifPresent(s -> {
			throw new FileStorageException("Job already saved");
		});

		SavedJob savedJob = new SavedJob();
		savedJob.setJobSeeker(jobSeeker);
		savedJob.setJob(job);

		savedJobRepository.save(savedJob);

		return ResponseEntity.ok("Job saved successfully");
	}

	// =========================
	// APPLY JOB
	// =========================
	public ResponseEntity<String> applyJob(Long userId, Long jobId) {

		Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));

		JobSeeker jobSeeker = jobSeekerRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("JobSeeker not found"));

		if (applicationRepository.findByJobAndJobSeeker(job, jobSeeker).isPresent()) {
			throw new RuntimeException("Already applied for this job");
		}

		Application app = new Application();
		app.setJob(job);
		app.setJobSeeker(jobSeeker);
		app.setStatus(Application.ApplicationStatus.APPLIED);
		app.setAppliedDate(LocalDateTime.now());

		// Capture current resume for one-click apply
		if (jobSeeker.getResumeFile() != null) {
			app.setResumePath(jobSeeker.getResumeFile());
		}

		applicationRepository.save(app);

		// Notify Employer
		notificationService.createNotification(
				job.getEmployer().getUserId(),
				"New Job Application",
				jobSeeker.getFullName() + " has applied for your job: " + job.getTitle(),
				"APPLICATION_UPDATE",
				"/employer/applicant/" + app.getId() // Link to applicant details
		);

		return ResponseEntity.ok("Application Submitted Successfully!");
	}

	// =========================
	// GET ALL JOBS
	// =========================
	public List<Job> getAllJobs() {
		return jobRepository.findAll();
	}

	// =========================
	// GET SAVED JOBS
	// =========================
	public List<SavedJob> getSavedJobsList(Long jobSeekerId) {

		JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
				.orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

		return savedJobRepository.findByJobSeeker(jobSeeker);
	}

	// REMOVE SAVED JOB
	// =========================
	public ResponseEntity<?> removeSavedJob(Long jobSeekerId, Long jobId) {

		JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
				.orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

		Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));

		SavedJob savedJob = savedJobRepository.findByJobSeekerAndJob(jobSeeker, job)
				.orElseThrow(() -> new ResourceNotFoundException("Saved job not found"));

		savedJobRepository.delete(savedJob);

		return ResponseEntity.ok("Removed from saved jobs");
	}

	// =========================
	// GET APPLICATIONS
	// =========================
	public List<Application> getApplicationsList(Long jobSeekerId) {
		return applicationRepository.findByJobSeeker_UserId(jobSeekerId);
	}

	// =========================
	// GET JOB SEEKER BY EMAIL ⭐⭐⭐
	// =========================
	public JobSeeker getJobSeekerByEmail(String email) {

		return jobSeekerRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));
	}

	// =========================
	// GET JOB BY ID
	// =========================
	public Job getJobById(Long jobId) {
		return jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
	}

	// =========================
	// WITHDRAW APPLICATION
	// =========================
	public void withdrawApplication(Long applicationId, String notes) {

		Application application = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		// Cannot withdraw if already withdrawn or rejected
		Application.ApplicationStatus current = application.getStatus();
		if (current == Application.ApplicationStatus.WITHDRAWN) {
			throw new RuntimeException("Application has already been withdrawn.");
		}
		if (current == Application.ApplicationStatus.REJECTED) {
			throw new RuntimeException("Cannot withdraw a rejected application.");
		}

		application.setStatus(Application.ApplicationStatus.WITHDRAWN);
		application.setNotes(notes != null ? notes : "");

		applicationRepository.save(application);
	}

	public void addSkill(Long jobSeekerId, String skillName) {

		JobSeeker js = jobSeekerRepository.findById(jobSeekerId)
				.orElseThrow(() -> new RuntimeException("JobSeeker not found"));

		Skill skill = new Skill();
		skill.setName(skillName);
		skill.setJobSeeker(js);

		skillRepository.save(skill);
	}

	public void deleteSkill(Long skillId) {
		skillRepository.deleteById(skillId);
	}

	public void addEducation(Long jobSeekerId, String degree, String institution) {

		JobSeeker js = jobSeekerRepository.findById(jobSeekerId)
				.orElseThrow(() -> new RuntimeException("JobSeeker not found"));

		Education edu = new Education();
		edu.setDegree(degree);
		edu.setInstitution(institution);
		edu.setJobSeeker(js);

		educationRepository.save(edu);
	}

	public void deleteEducation(Long educationId) {
		educationRepository.deleteById(educationId);
	}

	public void addCertification(Long jobSeekerId, String name, String issuer, String year) {

		JobSeeker js = jobSeekerRepository.findById(jobSeekerId)
				.orElseThrow(() -> new RuntimeException("JobSeeker not found"));

		Certification cert = new Certification();
		cert.setName(name);
		cert.setIssuer(issuer);
		cert.setYear(year);
		cert.setJobSeeker(js);

		certificationRepository.save(cert);
	}

	public void deleteCertification(Long certId) {
		certificationRepository.deleteById(certId);
	}

	public List<JobSeeker> searchJobSeekersByResumeContent(String keyword) {
		return jobSeekerRepository.findAll().stream().filter(js -> js.getResumeText() != null)
				.filter(js -> js.getResumeText().toLowerCase().contains(keyword.toLowerCase()))
				.collect(Collectors.toList());
	}
}