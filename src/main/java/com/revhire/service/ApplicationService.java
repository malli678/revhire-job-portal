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

@Service
public class ApplicationService {

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobSeekerRepository jobSeekerRepository;
	@Autowired
	private FileStorageService fileStorageService;

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

	    System.out.println("APPLICATION SAVED ✅");
	}


	// WITHDRAW
	public void withdrawApplication(Long applicationId, String notes) {

	    Application application = applicationRepository.findById(applicationId)
	            .orElseThrow(() -> new RuntimeException("Application not found"));

	    if (application.getStatus() == Application.ApplicationStatus.APPLIED) {

	        application.setStatus(Application.ApplicationStatus.WITHDRAWN);

	        // ✅ SAVE REASON ⭐⭐⭐
	        application.setNotes(notes);

	        applicationRepository.save(application);

	    } else {
	        throw new RuntimeException("Cannot withdraw");
	    }
	}

	// SHORTLIST
	public void shortlistCandidate(Long applicationId, String note) {

		Application app = applicationRepository.findById(applicationId).orElseThrow();

		app.setStatus(Application.ApplicationStatus.SHORTLISTED);
		app.setNotes(note);

		applicationRepository.save(app);
	}

	// REJECT
	public void rejectCandidate(Long applicationId, String notes) {

		Application app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		app.setStatus(Application.ApplicationStatus.REJECTED);
		app.setNotes(notes);

		applicationRepository.save(app);
	}

	// UPDATE STATUS
	public void updateApplicationStatus(Long applicationId, Application.ApplicationStatus status) {

		Application application = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		application.setStatus(status);
		applicationRepository.save(application);
	}

	// BULK UPDATE
	public void bulkUpdate(List<Long> applicationIds, String action) {

		List<Application> applications = applicationRepository.findAllById(applicationIds);

		for (Application app : applications) {

			if ("SHORTLIST".equalsIgnoreCase(action)) {
				app.setStatus(Application.ApplicationStatus.SHORTLISTED);
			}

			if ("REJECT".equalsIgnoreCase(action)) {
				app.setStatus(Application.ApplicationStatus.REJECTED);
			}
		}

		applicationRepository.saveAll(applications);
	}

	// ADD NOTES
	public void addNotes(Long applicationId, String notes) {

		Application application = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		application.setNotes(notes);
		applicationRepository.save(application);
	}

	// Get applications by job seeker ID
	public List<Application> getApplicationsByJobSeeker(Long jobSeekerId) {
	    return applicationRepository.findByJobSeeker_UserId(jobSeekerId);
	}

	// Get applications by job ID
	public List<Application> getApplicationsByJob(Long jobId) {
	    return applicationRepository.findByJob_JobId(jobId);
	}

	// Check if already applied
	public boolean hasAlreadyApplied(Long jobId, Long jobSeekerId) {
	    return applicationRepository
	        .findByJob_JobIdAndJobSeeker_UserId(jobId, jobSeekerId)
	        .isPresent();
	}

	// bulk update status
	public void bulkUpdateStatus(List<Long> applicationIds, Application.ApplicationStatus newStatus, String note) {

		List<Application> applications = applicationRepository.findAllById(applicationIds);

		for (Application app : applications) {

			app.setStatus(newStatus);

			if (newStatus == Application.ApplicationStatus.REJECTED) {
				app.setNotes(note);
			}
		}

		applicationRepository.saveAll(applications);
	}
	//filter and count
	public List<Application> getApplicationsForEmployer(Employer employer) {
	    return applicationRepository.findAll()
	            .stream()
	            .filter(app -> app.getJob().getEmployer().equals(employer))
	            .toList();
	}

	public List<Application> filterByStatus(Employer employer,
	                                        Application.ApplicationStatus status) {
	    return applicationRepository.findAll()
        .stream()
        .filter(app -> app.getJob().getEmployer().equals(employer))
        .filter(app -> app.getStatus() == status)
        .toList();
	}

	public List<Application> filterByExperience(Employer employer, Integer years) {
	    return applicationRepository.findAll()
	            .stream()
	            .filter(app -> app.getJob().getEmployer().equals(employer))
	            .filter(app -> app.getJobSeeker().getTotalExperienceYears() >= years)
	            .toList();
	}

	public List<Application> filterByEducation(Employer employer, String degree) {
	    return applicationRepository.findAll()
	            .stream()
	            .filter(app -> app.getJob().getEmployer().equals(employer))
	            .filter(app -> degree.equalsIgnoreCase(app.getJobSeeker().getDegree()))
	            .toList();
	}

	public List<Application> filterByDate(Employer employer, LocalDateTime date) {
	    return applicationRepository.findAll()
	            .stream()
	            .filter(app -> app.getJob().getEmployer().equals(employer))
	            .filter(app -> app.getAppliedDate().isAfter(date))
	            .toList();
	}
	public void apply(Long jobId,
            MultipartFile resume,
            String coverLetter,
            String email){

Job job = jobRepository.findById(jobId)
.orElseThrow(() -> new RuntimeException("Job not found"));

JobSeeker jobSeeker = jobSeekerRepository.findByEmail(email)
.orElseThrow(() -> new RuntimeException("JobSeeker not found"));

if (applicationRepository.findByJobAndJobSeeker(job, jobSeeker).isPresent()) {
throw new RuntimeException("Already applied");
}

// ✅ SAVE FILE
String resumeFileName = fileStorageService.storeFile(resume);

Application app = new Application();
app.setJob(job);
app.setJobSeeker(jobSeeker);
app.setResumePath(resumeFileName);   // store filename
app.setStatus(Application.ApplicationStatus.APPLIED);
app.setAppliedDate(LocalDateTime.now());

applicationRepository.save(app);
}
}