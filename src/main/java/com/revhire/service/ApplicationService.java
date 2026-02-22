package com.revhire.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.JobRepository;
import com.revhire.repository.JobSeekerRepository;

import com.revhire.model.Application;
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

    // APPLY JOB
    public void applyJob(Long jobId, Long jobSeekerId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new RuntimeException("JobSeeker not found"));

        if (applicationRepository.findByJobAndJobSeeker(job, jobSeeker).isPresent()) {
            throw new RuntimeException("Already applied for this job");
        }

        Application application = new Application();
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setStatus(Application.ApplicationStatus.APPLIED);
        application.setAppliedDate(LocalDateTime.now());

        applicationRepository.save(application);
    }

    // WITHDRAW
    public void withdrawApplication(Long applicationId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (application.getStatus() == Application.ApplicationStatus.APPLIED) {
            application.setStatus(Application.ApplicationStatus.WITHDRAWN);
            applicationRepository.save(application);
        } else {
            throw new RuntimeException("Cannot withdraw");
        }
    }

    // SHORTLIST
    public void shortlistCandidate(Long applicationId) {
        updateApplicationStatus(applicationId,
                Application.ApplicationStatus.SHORTLISTED);
    }

    // REJECT
    public void rejectCandidate(Long applicationId) {
        updateApplicationStatus(applicationId,
                Application.ApplicationStatus.REJECTED);
    }

    // UPDATE STATUS
    public void updateApplicationStatus(Long applicationId,
                                        Application.ApplicationStatus status) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(status);
        applicationRepository.save(application);
    }

    // BULK UPDATE
    public void bulkUpdateStatus(List<Long> applicationIds,
                                 Application.ApplicationStatus status) {

        List<Application> applications =
                applicationRepository.findAllById(applicationIds);

        for (Application app : applications) {
            app.setStatus(status);
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

    // VIEW BY JOB SEEKER
    public List<Application> getApplicationsByJobSeeker(Long jobSeekerId) {

        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new RuntimeException("JobSeeker not found"));

        return applicationRepository.findByJobSeeker(jobSeeker);
    }

    // VIEW BY JOB
    public List<Application> getApplicationsByJob(Long jobId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        return applicationRepository.findByJob(job);
    }
}