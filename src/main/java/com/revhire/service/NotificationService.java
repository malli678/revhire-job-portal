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

/**
 * Service class responsible for managing all notification related operations.
 *
 * This service handles:
 * - Creating notifications
 * - Sending application status updates
 * - Sending job recommendation notifications
 * - Retrieving notifications
 * - Marking notifications as read
 * - Deleting notifications
 *
 * The service communicates with:
 * - NotificationRepository for database operations
 * - UserRepository for retrieving users
 *
 * Notifications are used to inform users about important events such as:
 * - Job application updates
 * - Job recommendations
 * - Application submissions
 * - System alerts
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Constructor based dependency injection.
     *
     * @param notificationRepository repository used to manage notification data
     * @param userRepository repository used to fetch user data
     */
    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates and stores a new notification for a specific user.
     *
     * This method runs asynchronously so it does not block
     * the main application thread.
     *
     * @param userId ID of the user receiving the notification
     * @param title short title of the notification
     * @param message detailed notification message
     * @param notificationType type of notification
     * @param link redirect URL when notification is clicked
     */
    @Async
    @Transactional
    public void createNotification(Long userId, String title, String message,
                                   String notificationType, String link) {
        try {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Notification notification = new Notification(
                    user,
                    title,
                    message,
                    notificationType,
                    link
            );

            notificationRepository.save(notification);

            log.info("Notification created for user {}: {}", userId, title);

        } catch (Exception e) {
            log.error("Failed to create notification for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Overloaded method for creating a general notification.
     *
     * Default notification type is "GENERAL".
     *
     * @param userId user receiving the notification
     * @param title notification title
     * @param message notification message
     * @param link redirect link
     */
    @Async
    public void createNotification(Long userId, String title, String message, String link) {
        createNotification(userId, title, message, "GENERAL", link);
    }

    /**
     * Sends notification when application status changes.
     *
     * Example:
     * APPLIED → SHORTLISTED
     * SHORTLISTED → REJECTED
     *
     * @param jobSeekerId ID of job seeker
     * @param jobTitle title of job applied
     * @param oldStatus previous application status
     * @param newStatus updated application status
     * @param notes optional employer notes
     * @param jobId job identifier
     */
    @Async
    public void notifyApplicationStatusChange(Long jobSeekerId,
                                              String jobTitle,
                                              String oldStatus,
                                              String newStatus,
                                              String notes,
                                              Long jobId) {

        String title = "Application Status Updated";

        String message = String.format(
                "Your application for '%s' has been moved from %s to %s%s",
                jobTitle,
                oldStatus,
                newStatus,
                notes != null && !notes.isEmpty()
                        ? ". Reason: " + notes
                        : ""
        );

        String link = "/jobseeker/applications";

        createNotification(jobSeekerId, title, message, "APPLICATION_UPDATE", link);
    }

    /**
     * Sends job recommendation notification to job seeker.
     *
     * Triggered when a new job matches the user's profile.
     *
     * @param jobSeekerId job seeker receiving recommendation
     * @param jobTitle job title
     * @param companyName company posting job
     * @param jobId job identifier
     */
    @Async
    public void notifyJobRecommendation(Long jobSeekerId,
                                        String jobTitle,
                                        String companyName,
                                        Long jobId) {

        String title = "Job Recommendation";

        String message = String.format(
                "New job matching your profile: %s at %s",
                jobTitle,
                companyName
        );

        String link = "/jobseeker/job/" + jobId;

        createNotification(jobSeekerId, title, message, "JOB_RECOMMENDATION", link);
    }

    /**
     * Sends job recommendations to multiple job seekers.
     *
     * Used when a job matches multiple candidates.
     *
     * @param jobSeekerIds list of job seeker IDs
     * @param jobTitle job title
     * @param companyName employer company name
     * @param jobId job identifier
     */
    @Async
    @Transactional
    public void sendBulkJobRecommendations(List<Long> jobSeekerIds,
                                           String jobTitle,
                                           String companyName,
                                           Long jobId) {

        for (Long seekerId : jobSeekerIds) {
            notifyJobRecommendation(seekerId, jobTitle, companyName, jobId);
        }
    }

    /**
     * Retrieves recent notifications for a user with limit.
     *
     * @param userId user identifier
     * @param limit maximum notifications to return
     * @return list of notifications ordered by newest first
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId, int limit) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(0, limit);

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Retrieves all notifications for a specific user.
     *
     * @param userId user identifier
     * @return list of notifications
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Retrieves all unread notifications for a user.
     *
     * @param userId user identifier
     * @return list of unread notifications
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Returns the number of unread notifications for a user.
     *
     * Used by the notification bell in the header.
     *
     * @param userId user identifier
     * @return unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Marks a single notification as read.
     *
     * @param notificationId notification identifier
     */
    @Transactional
    public void markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notification.setUpdatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    /**
     * Marks all notifications as read for a specific user.
     *
     * @param userId user identifier
     * @return number of notifications updated
     */
    @Transactional
    public int markAllAsRead(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.markAllAsRead(user, LocalDateTime.now());
    }

    /**
     * Deletes a specific notification.
     *
     * @param notificationId notification identifier
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Deletes old notifications older than specified days.
     *
     * Useful for cleaning old notifications from database.
     *
     * @param daysOld number of days
     * @return number of notifications deleted
     */
    @Transactional
    public int deleteOldNotifications(int daysOld) {

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

        return notificationRepository.deleteOldNotifications(cutoffDate);
    }
}