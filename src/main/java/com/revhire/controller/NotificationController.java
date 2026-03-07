package com.revhire.controller;

import com.revhire.model.Notification;
import com.revhire.service.NotificationService;
import com.revhire.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for handling all notification related operations.
 *
 * This controller provides endpoints for:
 * - Viewing notifications page
 * - Fetching unread notification count
 * - Fetching recent notifications
 * - Marking notifications as read
 * - Marking all notifications as read
 * - Deleting notifications
 *
 * Notifications are used to inform users about events such as:
 * - Job application submitted
 * - Application shortlisted
 * - Application rejected
 * - Application withdrawn
 *
 * The controller interacts with NotificationService to perform
 * all notification related business logic.
 */
@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * Constructor based dependency injection.
     *
     * @param notificationService service responsible for notification logic
     * @param userService service responsible for user operations
     */
    public NotificationController(NotificationService notificationService,
                                  UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    /**
     * Displays the notifications page for the logged-in user.
     *
     * This method retrieves all notifications for the authenticated user
     * and the number of unread notifications.
     *
     * @param model Spring Model used to pass data to Thymeleaf view
     * @param authentication Spring Security authentication object
     * @return notifications page view
     */
    @GetMapping
    public String viewNotifications(Model model, Authentication authentication) {

        Long userId = userService.findByEmail(authentication.getName()).getUserId();

        List<Notification> notifications =
                notificationService.getUserNotifications(userId);

        long unreadCount =
                notificationService.getUnreadCount(userId);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "notifications";
    }

    /**
     * Returns the unread notification count for the logged-in user.
     *
     * This endpoint is used by the notification bell in the header
     * to show the number of unread notifications.
     *
     * @param authentication authenticated user
     * @return JSON response containing unread count
     */
    @GetMapping("/unread/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {

        Long userId = userService.findByEmail(authentication.getName()).getUserId();

        long count = notificationService.getUnreadCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves recent notifications for the logged-in user.
     *
     * Used by the notification dropdown in the header to display
     * latest notifications without loading the entire page.
     *
     * @param authentication authenticated user
     * @param limit maximum number of notifications to return
     * @return list of recent notifications
     */
    @GetMapping("/recent")
    @ResponseBody
    public ResponseEntity<List<Notification>> getRecentNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {

        Long userId = userService.findByEmail(authentication.getName()).getUserId();

        List<Notification> notifications =
                notificationService.getUserNotifications(userId, limit);

        return ResponseEntity.ok(notifications);
    }

    /**
     * Marks a specific notification as read.
     *
     * This is typically triggered when a user clicks a notification.
     *
     * @param id notification id
     * @return success response
     */
    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {

        notificationService.markAsRead(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    /**
     * Marks all notifications as read for the logged-in user.
     *
     * This endpoint is used when user clicks "Mark all as read".
     *
     * @param authentication authenticated user
     * @return response containing number of notifications updated
     */
    @PostMapping("/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {

        Long userId = userService.findByEmail(authentication.getName()).getUserId();

        int count = notificationService.markAllAsRead(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a notification.
     *
     * Allows users to remove notifications they no longer need.
     *
     * @param id notification id
     * @return success response
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {

        notificationService.deleteNotification(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        return ResponseEntity.ok(response);
    }
}