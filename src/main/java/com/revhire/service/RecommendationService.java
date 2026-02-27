
package com.revhire.service;

import com.revhire.model.Job;
import com.revhire.model.JobSeeker;
import com.revhire.model.Skill;
import com.revhire.repository.JobRepository;
import com.revhire.repository.JobSeekerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    
    private final JobRepository jobRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final NotificationService notificationService;
    
    public RecommendationService(JobRepository jobRepository,
                                 JobSeekerRepository jobSeekerRepository,
                                 NotificationService notificationService) {
        this.jobRepository = jobRepository;
        this.jobSeekerRepository = jobSeekerRepository;
        this.notificationService = notificationService;
    }
    
    // =========================
    // GET RECOMMENDED JOBS FOR USER
    // =========================
    public List<Job> getRecommendedJobs(Long jobSeekerId, int limit) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new RuntimeException("JobSeeker not found"));
        
        // Get user's skills
        Set<String> userSkills = jobSeeker.getSkills();
        if (userSkills == null || userSkills.isEmpty()) {
            return jobRepository.findTop10ByOrderByPostedDateDesc();
        }
        
        // Get all active jobs
        List<Job> allJobs = jobRepository.findByStatus("ACTIVE");
        
        // Score each job based on relevance
        Map<Job, Integer> jobScores = new HashMap<>();
        
        for (Job job : allJobs) {
            int score = 0;
            
            // Skill match (highest weight)
            if (job.getSkillsRequired() != null) {
                String[] jobSkills = job.getSkillsRequired().split(",");
                for (String jobSkill : jobSkills) {
                    if (userSkills.stream().anyMatch(s -> 
                        s.trim().equalsIgnoreCase(jobSkill.trim()))) {
                        score += 10;
                    }
                }
            }
            
            // Title match
            if (job.getTitle() != null && jobSeeker.getDesignation() != null) {
                if (job.getTitle().toLowerCase().contains(jobSeeker.getDesignation().toLowerCase())) {
                    score += 5;
                }
            }
            
            // Location match
            if (job.getLocation() != null && jobSeeker.getLocation() != null) {
                if (job.getLocation().equalsIgnoreCase(jobSeeker.getLocation())) {
                    score += 3;
                }
            }
            
            // Experience level match
            if (job.getExperienceRequired() != null && jobSeeker.getTotalExperienceYears() != null) {
                try {
                    int reqExp = Integer.parseInt(job.getExperienceRequired().replaceAll("[^0-9]", ""));
                    int userExp = jobSeeker.getTotalExperienceYears();
                    if (Math.abs(reqExp - userExp) <= 2) {
                        score += 2;
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
            
            if (score > 0) {
                jobScores.put(job, score);
            }
        }
        
        // Sort by score and return top N
        return jobScores.entrySet().stream()
                .sorted(Map.Entry.<Job, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    // =========================
    // SEND RECOMMENDATION NOTIFICATIONS (RUNS DAILY)
    // =========================
    @Scheduled(cron = "0 0 9 * * *") // Runs at 9 AM every day
    public void sendDailyRecommendations() {
        List<JobSeeker> allJobSeekers = jobSeekerRepository.findAll();
        
        for (JobSeeker seeker : allJobSeekers) {
            List<Job> recommendations = getRecommendedJobs(seeker.getUserId(), 3);
            
            for (Job job : recommendations) {
                notificationService.notifyJobRecommendation(
                    seeker.getUserId(),
                    job.getTitle(),
                    job.getEmployer().getCompanyName(),
                    job.getJobId()
                );
            }
        }
    }
    
    // =========================
    // SEND RECOMMENDATIONS FOR NEW JOB
    // =========================
    public void notifyMatchingJobSeekers(Job job) {
        List<JobSeeker> allJobSeekers = jobSeekerRepository.findAll();
        
        for (JobSeeker seeker : allJobSeekers) {
            if (isJobRelevantForSeeker(job, seeker)) {
                notificationService.notifyJobRecommendation(
                    seeker.getUserId(),
                    job.getTitle(),
                    job.getEmployer().getCompanyName(),
                    job.getJobId()
                );
            }
        }
    }
    
    private boolean isJobRelevantForSeeker(Job job, JobSeeker seeker) {
        // Check if job matches seeker's skills or designation
        if (seeker.getSkills() != null && job.getSkillsRequired() != null) {
            String[] jobSkills = job.getSkillsRequired().split(",");
            for (String jobSkill : jobSkills) {
                if (seeker.getSkills().stream().anyMatch(s -> 
                    s.trim().equalsIgnoreCase(jobSkill.trim()))) {
                    return true;
                }
            }
        }
        
        if (seeker.getDesignation() != null && job.getTitle() != null) {
            return job.getTitle().toLowerCase().contains(seeker.getDesignation().toLowerCase());
        }
        
        return false;
    }
}
