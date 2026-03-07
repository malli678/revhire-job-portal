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

/**
 * Repository interface for managing Notification entities.
 *
 * This repository extends JpaRepository which provides
 * basic CRUD operations automatically such as:
 *
 * - save()
 * - findById()
 * - findAll()
 * - deleteById()
 *
 * In addition to default JPA methods, this repository
 * defines several custom query methods used for:
 *
 * - retrieving notifications for a user
 * - retrieving unread notifications
 * - counting unread notifications
 * - pagination of notifications
 * - bulk updating notifications
 * - deleting old notifications
 *
 * Spring Data JPA automatically generates SQL queries
 * for methods that follow naming conventions.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Retrieves all notifications for a specific user.
     *
     * Notifications are sorted in descending order
     * based on creation time so newest notifications appear first.
     *
     * @param user user whose notifications should be retrieved
     * @return list of notifications ordered by newest first
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Retrieves all unread notifications for a user.
     *
     * This is used for:
     * - unread notification dropdown
     * - unread notification list
     *
     * @param user user whose unread notifications should be retrieved
     * @return list of unread notifications ordered by newest first
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /**
     * Counts the number of unread notifications for a user.
     *
     * This method is used by the notification bell
     * to display the unread notification badge.
     *
     * @param user user whose unread notifications are counted
     * @return number of unread notifications
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Retrieves recent notifications with pagination support.
     *
     * This is used when showing limited notifications
     * in a dropdown menu.
     *
     * Example:
     * Show only latest 5 notifications.
     *
     * @param user user whose notifications are retrieved
     * @param pageable pagination configuration
     * @return list of paginated notifications
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Retrieves notifications of a specific type for a user.
     *
     * Example types:
     * - APPLICATION_UPDATE
     * - JOB_RECOMMENDATION
     * - GENERAL
     *
     * @param user user receiving notifications
     * @param type notification type
     * @return list of notifications filtered by type
     */
    List<Notification> findByUserAndNotificationTypeOrderByCreatedAtDesc(User user, String type);

    /**
     * Marks all unread notifications as read for a specific user.
     *
     * This operation updates multiple records at once
     * using a JPQL update query.
     *
     * @param user user whose notifications should be marked as read
     * @param now timestamp for last update
     * @return number of notifications updated
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = :now WHERE n.user = :user AND n.isRead = false")
    int markAllAsRead(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Deletes notifications older than a given date.
     *
     * This is used to clean up old notifications
     * from the database.
     *
     * Example:
     * Delete notifications older than 30 days.
     *
     * @param cutoffDate date threshold
     * @return number of deleted notifications
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}