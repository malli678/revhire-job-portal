package com.revhire.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.JobRepository;
import com.revhire.repository.JobSeekerRepository;

import com.revhire.model.Application;
import com.revhire.model.Application.ApplicationStatus;
import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.model.JobSeeker;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 * ApplicationService handles the business logic related to job applications.
 *
 * Responsibilities:
 * - Allow job seekers to apply for jobs.
 * - Manage application status updates (shortlist, reject, withdraw, review).
 * - Handle bulk operations on applications.
 * - Store resumes and cover letters.
 * - Send email notifications.
 * - Create in-app notifications.
 *
 * This service acts as the middle layer between controllers
 * and the database repositories.
 */

@Service
@Transactional
public class ApplicationService {

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobSeekerRepository jobSeekerRepository;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private EmailService emailService;

	/**
	 * Service used to generate in-app notifications.
	 */
	@Autowired
	private NotificationService notificationService;
	
	/**
	 * Allows a job seeker to apply for a job.
	 *
	 * Steps performed:
	 * - Validate job and job seeker existence
	 * - Check if the user already applied
	 * - Create a new application
	 * - Save application in database
	 * - Send notifications to job seeker and employer
	 */

	// APPLY JOB
	public void applyJob(Long jobId, Long jobSeekerId) {
		System.out.println("JOB ID = " + jobId);
		System.out.println("JOB SEEKER ID = " + jobSeekerId);

		Job job = jobRepository.findById(jobId)
				.orElseThrow(() -> new RuntimeException("Job not found"));

		JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
				.orElseThrow(() -> new RuntimeException("JobSeeker not found"));

		System.out.println("JOB = " + job.getTitle());
		System.out.println("JOB SEEKER = " + jobSeeker.getFullName());

		if (applicationRepository.findByJobAndJobSeeker(job, jobSeeker).isPresent()) {
			System.out.println("DUPLICATE FOUND ⚠️");
			throw new RuntimeException("Already applied for this job");
		}

		Application application = new Application();
		application.setJob(job);
		application.setJobSeeker(jobSeeker);
		application.setStatus(Application.ApplicationStatus.APPLIED);
		application.setAppliedDate(LocalDateTime.now());

		applicationRepository.save(application);

		// Create notifications
		notificationService.createNotification(
				jobSeeker.getUserId(),
				"Application Submitted",
				"Your application for " + job.getTitle() + " has been submitted successfully.",
				"/jobseeker/applications");

		notificationService.createNotification(
				job.getEmployer().getUserId(),
				"New Application Received",
				jobSeeker.getFullName() + " applied for " + job.getTitle(),
				"/employer/applicants?jobId=" + job.getJobId());

		System.out.println("APPLICATION SAVED ✅");
	}

	/**
	 * Allows a job seeker to withdraw their application.
	 */
	// WITHDRAW
	public void withdrawApplication(Long applicationId, String notes) {
		Application application = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		if (application.getStatus() == Application.ApplicationStatus.APPLIED) {
			application.setStatus(Application.ApplicationStatus.WITHDRAWN);
			application.setNotes(notes);
			applicationRepository.save(application);

			// ✅ Create notification for withdrawal
			notificationService.createNotification(
					application.getJobSeeker().getUserId(),
					"Application Withdrawn",
					"You have withdrawn your application for " + application.getJob().getTitle(),
					"/jobseeker/applications");
		} else {
			throw new RuntimeException("Cannot withdraw");
		}
	}


	/**
	 * Shortlists a candidate for the job.
	 */
	// SHORTLIST
	public void shortlistCandidate(Long applicationId, String note) {
		Application app = applicationRepository.findById(applicationId).orElseThrow();

		if (app.getStatus() == ApplicationStatus.WITHDRAWN) {
			throw new RuntimeException("Cannot update a withdrawn application.");
		}

		String oldStatus = app.getStatus().toString();
		app.setStatus(Application.ApplicationStatus.SHORTLISTED);
		app.setNotes(note);
		applicationRepository.save(app);
		emailService.sendApplicationStatusUpdate(app, oldStatus);
		//  Create notification for shortlist
		notificationService.createNotification(
				app.getJobSeeker().getUserId(),
				"Application Shortlisted",
				"Congratulations! Your application for " + app.getJob().getTitle() + " has been shortlisted." +
						(note != null && !note.isEmpty() ? " Note: " + note : ""),
				"/jobseeker/applications");
	}

	/**
	 * Rejects a candidate application.
	 */
	// REJECT
	public void rejectCandidate(Long applicationId, String notes) {
		Application app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		if (app.getStatus() == ApplicationStatus.WITHDRAWN) {
			throw new RuntimeException("Cannot update a withdrawn application.");
		}

		String oldStatus = app.getStatus().toString();
		app.setStatus(Application.ApplicationStatus.REJECTED);
		app.setNotes(notes);
		applicationRepository.save(app);
		// Send email notification
		emailService.sendApplicationStatusUpdate(app, oldStatus);
		// Create notification for rejection
		notificationService.createNotification(
				app.getJobSeeker().getUserId(),
				"Application Update",
				"Your application for " + app.getJob().getTitle() + " has been reviewed." +
						(notes != null && !notes.isEmpty() ? " Feedback: " + notes : ""),
				"/jobseeker/applications");
	}

	/**
	 * Updates application status manually.
	 */
	// UPDATE STATUS
	public void updateApplicationStatus(Long applicationId, Application.ApplicationStatus status) {
		Application application = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		Application.ApplicationStatus oldStatus = application.getStatus();
		application.setStatus(status);
		applicationRepository.save(application);

		// Create notification for status change
		notificationService.createNotification(
				application.getJobSeeker().getUserId(),
				"Application Status Updated",
				"Your application for " + application.getJob().getTitle() +
						" has been updated from " + oldStatus + " to " + status,
				"/jobseeker/applications");
	}
	
	/**
	 * Performs bulk update operations such as SHORTLIST or REJECT.
	 */

	// BULK UPDATE
	public void bulkUpdate(List<Long> applicationIds, String action) {
		List<Application> applications = applicationRepository.findAllById(applicationIds);

		for (Application app : applications) {
			Application.ApplicationStatus oldStatus = app.getStatus();

			if ("SHORTLIST".equalsIgnoreCase(action)) {
				app.setStatus(Application.ApplicationStatus.SHORTLISTED);
			}
			if ("REJECT".equalsIgnoreCase(action)) {
				app.setStatus(Application.ApplicationStatus.REJECTED);
			}

			applicationRepository.save(app);

			// Create notification for bulk update
			notificationService.createNotification(
					app.getJobSeeker().getUserId(),
					"Application Status Updated",
					"Your application for " + app.getJob().getTitle() +
							" has been updated from " + oldStatus + " to " + app.getStatus(),
					"/jobseeker/applications");
		}
	}

	/**
	 * Adds employer notes to an application.
	 */
	// ADD NOTES
	public void addNotes(Long applicationId, String notes) {
		Application application = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		application.setNotes(notes);
		applicationRepository.save(application);
	}

	/**
	 * Retrieves applications submitted by a specific job seeker.
	 */
	// Get applications by job seeker ID
	public List<Application> getApplicationsByJobSeeker(Long jobSeekerId) {
		return applicationRepository.findByJobSeeker_UserId(jobSeekerId);
	}
	/**
	 * Retrieves all applications for a specific job.
	 */
	// Get applications by job ID
	public List<Application> getApplicationsByJob(Long jobId) {
		return applicationRepository.findByJob_JobId(jobId);
	}

	/**
	 * Checks whether a job seeker has already applied to a job.
	 */
	// Check if already applied
	public boolean hasAlreadyApplied(Long jobId, Long jobSeekerId) {
		return applicationRepository
				.findByJob_JobIdAndJobSeeker_UserId(jobId, jobSeekerId)
				.isPresent();
	}
	
	/**
	 * Performs bulk update of application statuses.
	 *
	 * This method updates multiple applications at once. It prevents updating
	 * applications that are already in terminal states such as SHORTLISTED,
	 * REJECTED, or WITHDRAWN.
	 *
	 * If notes are provided, they will be attached to the updated application.
	 * A notification is also created for each job seeker whose application status changes.
	 *
	 * @param applicationIds list of application IDs to update
	 * @param newStatus new status to apply
	 * @param note optional note from the employer
	 */

	// bulk update status
	public void bulkUpdateStatus(List<Long> applicationIds, Application.ApplicationStatus newStatus, String note) {
		List<Application> applications = applicationRepository.findAllById(applicationIds);

		for (Application app : applications) {
			// Rule: once terminal (SHORTLISTED or REJECTED) or WITHDRAWN, don't change
			// status via bulk
			if (app.getStatus() == ApplicationStatus.SHORTLISTED ||
					app.getStatus() == ApplicationStatus.REJECTED ||
					app.getStatus() == ApplicationStatus.WITHDRAWN) {
				continue;
			}

			Application.ApplicationStatus oldStatus = app.getStatus();
			app.setStatus(newStatus);

			// Apply notes if provided for any status
			if (note != null && !note.trim().isEmpty()) {
				app.setNotes(note);
			}

			applicationRepository.save(app);

			// Create notification for status change
			notificationService.createNotification(
					app.getJobSeeker().getUserId(),
					"Application Status Updated",
					"Your application for " + app.getJob().getTitle() +
							" has been updated from " + oldStatus + " to " + newStatus +
							(note != null && !note.isEmpty() ? " Note: " + note : ""),
					"/jobseeker/applications");
		}
	}
	
	/**
	 * Retrieves all applications submitted for jobs posted by a specific employer.
	 *
	 * @param employer employer whose applications need to be retrieved
	 * @return list of applications belonging to the employer's jobs
	 */

	// filter and count
	public List<Application> getApplicationsForEmployer(Employer employer) {
		return applicationRepository.findAll()
				.stream()
				.filter(app -> app.getJob().getEmployer().equals(employer))
				.toList();
	}

	
	/**
	 * Filters applications by status for a specific employer.
	 *
	 * @param employer employer whose applications are being filtered
	 * @param status application status to filter by
	 * @return filtered list of applications
	 */
	public List<Application> filterByStatus(Employer employer,
			Application.ApplicationStatus status) {
		return applicationRepository.findAll()
				.stream()
				.filter(app -> app.getJob().getEmployer().equals(employer))
				.filter(app -> app.getStatus() == status)
				.toList();
	}

	/**
	 * Filters applications based on minimum experience of job seekers.
	 *
	 * @param employer employer whose applications are being filtered
	 * @param years minimum experience required
	 * @return list of applications matching the experience criteria
	 */
	public List<Application> filterByExperience(Employer employer, Integer years) {
		return applicationRepository.findAll()
				.stream()
				.filter(app -> app.getJob().getEmployer().equals(employer))
				.filter(app -> app.getJobSeeker().getTotalExperienceYears() >= years)
				.toList();
	}
	
	/**
	 * Filters employer applications by job seeker education degree.
	 */

	public List<Application> filterByEducation(Employer employer, String degree) {
		return applicationRepository.findAll()
				.stream()
				.filter(app -> app.getJob().getEmployer().equals(employer))
				.filter(app -> degree.equalsIgnoreCase(app.getJobSeeker().getDegree()))
				.toList();
	}

	/**
	 * Filters employer applications submitted after a given date.
	 */
	public List<Application> filterByDate(Employer employer, LocalDateTime date) {
		return applicationRepository.findAll()
				.stream()
				.filter(app -> app.getJob().getEmployer().equals(employer))
				.filter(app -> app.getAppliedDate().isAfter(date))
				.toList();
	}

	/**
	 * Allows a job seeker to apply for a job with resume and cover letter.
	 *
	 * This method:
	 * - Validates job and job seeker existence
	 * - Checks if the user already applied
	 * - Stores the uploaded resume
	 * - Creates a new application record
	 * - Sends email notifications
	 * - Creates system notifications
	 *
	 * @param jobId job ID being applied for
	 * @param resume uploaded resume file
	 * @param coverLetter optional cover letter text
	 * @param email job seeker email
	 */
	// FIXED apply method with notification
	public void apply(Long jobId, MultipartFile resume, String coverLetter, String email) {
		Job job = jobRepository.findById(jobId)
				.orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

		JobSeeker jobSeeker = jobSeekerRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("JobSeeker not found with email: " + email));

		if (applicationRepository.findByJobAndJobSeeker(job, jobSeeker).isPresent()) {
			throw new RuntimeException("Already applied");
		}

		String resumeFileName = null;
		if (resume != null && !resume.isEmpty()) {
			resumeFileName = fileStorageService.storeFile(resume);
		} else if (jobSeeker.getResumeFile() != null && !jobSeeker.getResumeFile().isEmpty()) {
			resumeFileName = jobSeeker.getResumeFile(); // Use saved resume from profile
		} else if (jobSeeker.getResumePath() != null && !jobSeeker.getResumePath().isEmpty()) {
			resumeFileName = jobSeeker.getResumePath(); // Fallback if using resumePath property
		} else {
			throw new RuntimeException("Resume is required. Please upload one or save one to your profile.");
		}

		Application app = new Application();
		app.setJob(job);
		app.setJobSeeker(jobSeeker);

		// CRITICAL: Set the bidirectional relationship
		job.getApplications().add(app); // Add to job's applications list
		jobSeeker.getApplications().add(app); // Add to jobSeeker's applications list

		app.setResumePath(resumeFileName);
		app.setCoverLetter(coverLetter);
		app.setStatus(Application.ApplicationStatus.APPLIED);
		app.setAppliedDate(LocalDateTime.now());

		// Save with explicit flush to catch any issues
		applicationRepository.saveAndFlush(app);
		applicationRepository.save(app);

		// Send emails
		emailService.sendApplicationConfirmation(jobSeeker, job);
		emailService.sendNewApplicationAlert(app);
		// Create notifications
		notificationService.createNotification(
				jobSeeker.getUserId(),
				"Application Submitted",
				"Your application for " + job.getTitle() + " has been submitted successfully.",
				"/jobseeker/applications");

		notificationService.createNotification(
				job.getEmployer().getUserId(),
				"New Application Received",
				jobSeeker.getFullName() + " applied for " + job.getTitle(),
				"/employer/applicants");
	}
	// Add these methods to ApplicationService.java

	
	/**
	 * Moves an application from APPLIED status to UNDER_REVIEW.
	 *
	 * Only applications currently in APPLIED state can be moved to UNDER_REVIEW.
	 * A notification is sent to the job seeker.
	 *
	 * @param applicationId application ID
	 * @param notes optional employer notes
	 */
	public void moveToUnderReview(Long applicationId, String notes) {
		Application app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		if (app.getStatus() == ApplicationStatus.WITHDRAWN) {
			throw new RuntimeException("Cannot update a withdrawn application.");
		}

		if (app.getStatus() != ApplicationStatus.APPLIED) {
			throw new RuntimeException("Only APPLIED applications can be moved to UNDER_REVIEW");
		}

		app.setStatus(ApplicationStatus.UNDER_REVIEW);
		if (notes != null && !notes.isEmpty()) {
			app.setNotes(notes);
		}
		applicationRepository.save(app);

		// Create notification
		notificationService.createNotification(
				app.getJobSeeker().getUserId(),
				"Application Under Review",
				"Your application for " + app.getJob().getTitle() + " is now under review.",
				"/jobseeker/applications");
	}
	
	/**
	 * Moves an application from UNDER_REVIEW to SHORTLISTED.
	 *
	 * Only applications currently in UNDER_REVIEW state can be shortlisted.
	 * A notification is sent to the job seeker informing them about the shortlist.
	 *
	 * @param applicationId application ID
	 * @param notes optional employer notes
	 */

	public void moveFromUnderReviewToShortlisted(Long applicationId, String notes) {
		Application app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		if (app.getStatus() == ApplicationStatus.WITHDRAWN) {
			throw new RuntimeException("Cannot update a withdrawn application.");
		}

		if (app.getStatus() != ApplicationStatus.UNDER_REVIEW) {
			throw new RuntimeException("Only UNDER_REVIEW applications can be shortlisted");
		}

		app.setStatus(ApplicationStatus.SHORTLISTED);
		if (notes != null && !notes.isEmpty()) {
			app.setNotes(notes);
		}
		applicationRepository.save(app);

		// Create notification
		notificationService.createNotification(
				app.getJobSeeker().getUserId(),
				"Application Shortlisted!",
				"Congratulations! Your application for " + app.getJob().getTitle() + " has been shortlisted.",
				"/jobseeker/applications");
	}

	/**
	 * Retrieves an application by its ID.
	 */
	public Application getApplicationById(Long applicationId) {
		return applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));
	}

	/**
	 * Updates the status of an application and saves optional notes.
	 */
	public void updateStatus(Long applicationId, Application.ApplicationStatus status, String notes) {
		Application app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		// Rule: once terminal or withdrawn, don't change
		if (app.getStatus() == ApplicationStatus.SHORTLISTED ||
				app.getStatus() == ApplicationStatus.REJECTED ||
				app.getStatus() == ApplicationStatus.WITHDRAWN) {
			throw new RuntimeException("This application has already been processed or withdrawn.");
		}

		Application.ApplicationStatus oldStatus = app.getStatus();
		app.setStatus(status);
		if (notes != null && !notes.trim().isEmpty()) {
			app.setNotes(notes);
		}
		applicationRepository.save(app);

		// Create notification for status change
		notificationService.createNotification(
				app.getJobSeeker().getUserId(),
				"Application Status Updated",
				"Your application for " + app.getJob().getTitle() +
						" has been updated to " + status
						+ (notes != null && !notes.trim().isEmpty() ? ". Remark: " + notes : ""),
				"/jobseeker/applications");
	}
}