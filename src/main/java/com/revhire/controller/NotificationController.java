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

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    public NotificationController(NotificationService notificationService, 
                                  UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }
    
    // =========================
    // VIEW NOTIFICATIONS PAGE
    // =========================
    @GetMapping
    public String viewNotifications(Model model, Authentication authentication) {
        Long userId = userService.findByEmail(authentication.getName()).getUserId();
        
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        long unreadCount = notificationService.getUnreadCount(userId);
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        
        return "notifications";
    }
    
    // =========================
    // GET UNREAD COUNT (AJAX)
    // =========================
    @GetMapping("/unread/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        Long userId = userService.findByEmail(authentication.getName()).getUserId();
        
        long count = notificationService.getUnreadCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    // =========================
    // GET RECENT NOTIFICATIONS (AJAX)
    // =========================
    @GetMapping("/recent")
    @ResponseBody
    public ResponseEntity<List<Notification>> getRecentNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        
        Long userId = userService.findByEmail(authentication.getName()).getUserId();
        
        List<Notification> notifications = notificationService.getUserNotifications(userId, limit);
        
        return ResponseEntity.ok(notifications);
    }
    
    // =========================
    // MARK AS READ
    // =========================
    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    // =========================
    // MARK ALL AS READ
    // =========================
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
}