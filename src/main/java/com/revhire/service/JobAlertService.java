package com.revhire.service;

import com.revhire.model.Job;
import com.revhire.model.JobAlert;
import com.revhire.model.JobSeeker;
import com.revhire.repository.JobAlertRepository;
import com.revhire.repository.JobRepository;
import com.revhire.repository.JobSeekerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobAlertService {
    
    private static final Logger log = LoggerFactory.getLogger(JobAlertService.class);
    
    private final JobAlertRepository jobAlertRepository;
    private final JobRepository jobRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    
    public JobAlertService(JobAlertRepository jobAlertRepository,
                          JobRepository jobRepository,
                          JobSeekerRepository jobSeekerRepository,
                          EmailService emailService,
                          NotificationService notificationService) {
        this.jobAlertRepository = jobAlertRepository;
        this.jobRepository = jobRepository;
        this.jobSeekerRepository = jobSeekerRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }
    
    @Transactional
    public JobAlert createAlert(JobSeeker jobSeeker, String alertName, String keywords,
                                String location, String jobType, Integer minSalary, String frequency) {
        JobAlert alert = new JobAlert();
        alert.setJobSeeker(jobSeeker);
        alert.setAlertName(alertName);
        alert.setKeywords(keywords);
        alert.setLocation(location);
        alert.setJobType(jobType);
        alert.setMinSalary(minSalary);
        alert.setFrequency(frequency);
        alert.setActive(true);
        
        return jobAlertRepository.save(alert);
    }
    
    @Transactional
    public void updateAlert(Long alertId, JobAlert updatedAlert) {
        JobAlert alert = jobAlertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found"));
        
        alert.setAlertName(updatedAlert.getAlertName());
        alert.setKeywords(updatedAlert.getKeywords());
        alert.setLocation(updatedAlert.getLocation());
        alert.setJobType(updatedAlert.getJobType());
        alert.setMinSalary(updatedAlert.getMinSalary());
        alert.setFrequency(updatedAlert.getFrequency());
        
        jobAlertRepository.save(alert);
    }
    
    @Transactional
    public void toggleAlert(Long alertId, boolean active) {
        JobAlert alert = jobAlertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found"));
        
        alert.setActive(active);
        jobAlertRepository.save(alert);
    }
    
    @Transactional
    public void deleteAlert(Long alertId) {
        jobAlertRepository.deleteById(alertId);
    }
    
    public List<JobAlert> getUserAlerts(Long jobSeekerId) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
            .orElseThrow(() -> new RuntimeException("JobSeeker not found"));
        
        return jobAlertRepository.findByJobSeeker(jobSeeker);
    }
    
    // Check new jobs against alerts
    public void checkNewJobAgainstAlerts(Job newJob) {
        List<JobAlert> activeAlerts = jobAlertRepository.findAllActiveAlerts();
        
        for (JobAlert alert : activeAlerts) {
            if (matchesAlert(newJob, alert)) {
                sendJobAlert(alert, newJob);
            }
        }
    }
    
    private boolean matchesAlert(Job job, JobAlert alert) {
        // Check keywords
        if (alert.getKeywords() != null && !alert.getKeywords().isEmpty()) {
            String[] keywords = alert.getKeywords().toLowerCase().split(",");
            boolean keywordMatch = false;
            for (String keyword : keywords) {
                if (job.getTitle().toLowerCase().contains(keyword.trim()) ||
                    (job.getDescription() != null && 
                     job.getDescription().toLowerCase().contains(keyword.trim()))) {
                    keywordMatch = true;
                    break;
                }
            }
            if (!keywordMatch) return false;
        }
        
        // Check location
        if (alert.getLocation() != null && !alert.getLocation().isEmpty()) {
            if (!job.getLocation().toLowerCase().contains(alert.getLocation().toLowerCase())) {
                return false;
            }
        }
        
        // Check job type
        if (alert.getJobType() != null && !alert.getJobType().isEmpty()) {
            if (!alert.getJobType().equalsIgnoreCase(job.getJobType())) {
                return false;
            }
        }
        
        // Check salary
        if (alert.getMinSalary() != null && job.getSalaryMin() != null) {
            if (job.getSalaryMin() < alert.getMinSalary()) {
                return false;
            }
        }
        
        return true;
    }
    
    private void sendJobAlert(JobAlert alert, Job job) {
        // Send email
        emailService.sendJobAlert(alert.getJobSeeker(), job);
        
        // Send in-app notification
        notificationService.createNotification(
            alert.getJobSeeker().getUserId(),
            "New Job Alert: " + job.getTitle(),
            "A new job matching your alert '" + alert.getAlertName() + "' has been posted.",
            "/jobseeker/job/" + job.getJobId()
        );
        
        // Update last sent time
        alert.setLastSentAt(LocalDateTime.now());
        jobAlertRepository.save(alert);
        
        log.info("Job alert sent for alert: {} to user: {}", 
                 alert.getAlertId(), alert.getJobSeeker().getUserId());
    }
    
    // Scheduled jobs
    @Scheduled(cron = "0 0 8 * * *") // 8 AM daily
    @Transactional
    public void sendDailyAlerts() {
        log.info("Sending daily job alerts");
        List<JobAlert> dailyAlerts = jobAlertRepository.findByIsActiveTrueAndFrequency("DAILY");
        sendAlertsForPeriod(dailyAlerts, LocalDateTime.now().minusDays(1));
    }
    
    @Scheduled(cron = "0 0 9 * * MON") // 9 AM every Monday
    @Transactional
    public void sendWeeklyAlerts() {
        log.info("Sending weekly job alerts");
        List<JobAlert> weeklyAlerts = jobAlertRepository.findByIsActiveTrueAndFrequency("WEEKLY");
        sendAlertsForPeriod(weeklyAlerts, LocalDateTime.now().minusDays(7));
    }
    
    private void sendAlertsForPeriod(List<JobAlert> alerts, LocalDateTime since) {
        List<Job> newJobs = jobRepository.findByPostedDateAfter(since);
        
        for (JobAlert alert : alerts) {
            List<Job> matchingJobs = new ArrayList<>();
            for (Job job : newJobs) {
                if (matchesAlert(job, alert)) {
                    matchingJobs.add(job);
                }
            }
            
            if (!matchingJobs.isEmpty()) {
                // Send summary email
                sendAlertSummary(alert, matchingJobs);
            }
        }
    }
    
    private void sendAlertSummary(JobAlert alert, List<Job> matchingJobs) {
        // For simplicity, send first matching job
        // In production, you might want to send a summary email with all matches
        if (!matchingJobs.isEmpty()) {
            emailService.sendJobAlert(alert.getJobSeeker(), matchingJobs.get(0));
            
            notificationService.createNotification(
                alert.getJobSeeker().getUserId(),
                "Job Alert: " + matchingJobs.size() + " new jobs found",
                matchingJobs.size() + " new jobs match your alert '" + alert.getAlertName() + "'",
                "/jobseeker/search-jobs"
            );
            
            alert.setLastSentAt(LocalDateTime.now());
            jobAlertRepository.save(alert);
        }
    }
}