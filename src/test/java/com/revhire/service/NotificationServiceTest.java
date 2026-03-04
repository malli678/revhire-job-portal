package com.revhire.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revhire.model.Notification;
import com.revhire.model.User;
import com.revhire.repository.NotificationRepository;
import com.revhire.repository.UserRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @Before
    public void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("user@test.com");

        notification = new Notification(user, "Title", "Message", "GENERAL", "/link");
        notification.setNotificationId(100L);
    }

    @Test
    public void testCreateNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        notificationService.createNotification(1L, "Test", "Content", "/link");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testMarkAsRead() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(100L);

        assertEquals(true, notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    public void testGetUnreadCount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.countByUserAndIsReadFalse(user)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
    }

    @Test
    public void testMarkAllAsRead() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.markAllAsRead(any(), any())).thenReturn(3);

        int count = notificationService.markAllAsRead(1L);

        assertEquals(3, count);
    }
}
