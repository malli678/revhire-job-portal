package com.revhire.service;

import com.revhire.model.Notification;
import com.revhire.model.User;
import com.revhire.repository.NotificationRepository;
import com.revhire.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    public NotificationService(NotificationRepository notificationRepository, 
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }
    
    // =========================
    // CREATE NOTIFICATION
    // =========================
    @Async
    @Transactional
    public void createNotification(Long userId, String title, String message, 
                                   String notificationType, String link) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Notification notification = new Notification(user, title, message, 
                                                        notificationType, link);
            notificationRepository.save(notification);
            
            log.info("Notification created for user {}: {}", userId, title);
        } catch (Exception e) {
            log.error("Failed to create notification for user {}: {}", userId, e.getMessage());
        }
    }
    
    // Overloaded method for simpler calls
    @Async
    public void createNotification(Long userId, String title, String message, String link) {
        createNotification(userId, title, message, "GENERAL", link);
    }
    
    // =========================
    // APPLICATION STATUS NOTIFICATION
    // =========================
    @Async
    public void notifyApplicationStatusChange(Long jobSeekerId, String jobTitle, 
                                              String oldStatus, String newStatus, 
                                              String notes, Long jobId) {
        String title = "Application Status Updated";
        String message = String.format("Your application for '%s' has been moved from %s to %s%s",
                jobTitle, oldStatus, newStatus,
                notes != null && !notes.isEmpty() ? ". Reason: " + notes : "");
        
        String link = "/jobseeker/applications";
        
        createNotification(jobSeekerId, title, message, "APPLICATION_UPDATE", link);
    }
    
    // =========================
    // JOB RECOMMENDATION NOTIFICATION
    // =========================
    @Async
    public void notifyJobRecommendation(Long jobSeekerId, String jobTitle, 
                                        String companyName, Long jobId) {
        String title = "Job Recommendation";
        String message = String.format("New job matching your profile: %s at %s", 
                                      jobTitle, companyName);
        String link = "/jobseeker/job/" + jobId;
        
        createNotification(jobSeekerId, title, message, "JOB_RECOMMENDATION", link);
    }
    
    // =========================
    // BULK JOB RECOMMENDATIONS
    // =========================
    @Async
    @Transactional
    public void sendBulkJobRecommendations(List<Long> jobSeekerIds, String jobTitle, 
                                           String companyName, Long jobId) {
        for (Long seekerId : jobSeekerIds) {
            notifyJobRecommendation(seekerId, jobTitle, companyName, jobId);
        }
    }
    
    // =========================
    // GET NOTIFICATIONS
    // =========================
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(0, limit);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }
    
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
    
    // =========================
    // MARK AS READ
    // =========================
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setRead(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
    
    @Transactional
    public int markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.markAllAsRead(user, LocalDateTime.now());
    }
    
    // =========================
    // DELETE NOTIFICATIONS
    // =========================
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    @Transactional
    public int deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return notificationRepository.deleteOldNotifications(cutoffDate);
    }
}