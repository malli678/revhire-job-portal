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
        Integer userExperience = jobSeeker.getTotalExperienceYears() != null ? jobSeeker.getTotalExperienceYears() : 0;

        // Get all active jobs
        List<Job> allJobs = jobRepository.findByStatus("ACTIVE");

        // Score each job based on relevance
        Map<Job, Integer> jobScores = new HashMap<>();

        for (Job job : allJobs) {
            int score = 0;

            // Skill match (highest weight)
            if (job.getSkillsRequired() != null && userSkills != null) {
                String[] jobSkills = job.getSkillsRequired().split(",");
                for (String jobSkill : jobSkills) {
                    if (userSkills.stream().anyMatch(s -> s.trim().equalsIgnoreCase(jobSkill.trim()))) {
                        score += 10;
                    }
                }
            }

            // For freshers (0-1 year experience), show entry-level jobs
            if (userExperience <= 1) {
                if (job.getExperienceRequired() != null) {
                    String expStr = job.getExperienceRequired().toLowerCase();
                    if (expStr.contains("fresher") || expStr.contains("entry") ||
                            expStr.contains("0") || expStr.contains("1") ||
                            expStr.contains("intern")) {
                        score += 15; // Boost score for entry-level jobs
                    }
                }
            } else {
                // For experienced, match experience level
                if (job.getExperienceRequired() != null && userExperience != null) {
                    try {
                        String expStr = job.getExperienceRequired().replaceAll("[^0-9-]", "");
                        if (expStr.contains("-")) {
                            String[] parts = expStr.split("-");
                            int minExp = Integer.parseInt(parts[0].trim());
                            int maxExp = Integer.parseInt(parts[1].trim());
                            if (userExperience >= minExp && userExperience <= maxExp) {
                                score += 8;
                            }
                        } else {
                            int reqExp = Integer.parseInt(expStr);
                            if (Math.abs(reqExp - userExperience) <= 2) {
                                score += 8;
                            }
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
            }

            // Location match (bonus)
            if (job.getLocation() != null && jobSeeker.getLocation() != null) {
                if (job.getLocation().toLowerCase().contains(jobSeeker.getLocation().toLowerCase())) {
                    score += 5;
                }
            }

            if (score > 0) {
                jobScores.put(job, score);
            }
        }

        // If no matches found, return recent jobs
        if (jobScores.isEmpty()) {
            return jobRepository.findTop10ByStatusOrderByPostedDateDesc("ACTIVE");
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
                        job.getJobId());
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
                        job.getJobId());
            }
        }
    }

    private boolean isJobRelevantForSeeker(Job job, JobSeeker seeker) {
        // Check if job matches seeker's skills or designation
        if (seeker.getSkills() != null && job.getSkillsRequired() != null) {
            String[] jobSkills = job.getSkillsRequired().split(",");
            for (String jobSkill : jobSkills) {
                if (seeker.getSkills().stream().anyMatch(s -> s.trim().equalsIgnoreCase(jobSkill.trim()))) {
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