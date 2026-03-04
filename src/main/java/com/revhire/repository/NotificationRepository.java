package com.revhire.repository;

import com.revhire.model.Notification;
import com.revhire.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a user (most recent first)
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // Find unread notifications for a user
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Count unread notifications
    long countByUserAndIsReadFalse(User user);
    
    // Find recent notifications with pagination
    List<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find notifications by type
    List<Notification> findByUserAndNotificationTypeOrderByCreatedAtDesc(User user, String type);
    
    // Bulk mark as read
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = :now WHERE n.user = :user AND n.isRead = false")
    int markAllAsRead(@Param("user") User user, @Param("now") LocalDateTime now);
    
    // Delete old notifications (older than 30 days)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}