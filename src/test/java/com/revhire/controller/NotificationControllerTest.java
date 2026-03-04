package com.revhire.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;

import org.springframework.web.servlet.view.InternalResourceViewResolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.revhire.model.User;
import com.revhire.service.NotificationService;
import com.revhire.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationController notificationController;

    private User user;

    @Before
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setViewResolvers(viewResolver)
                .build();

        user = new User();
        user.setUserId(1L);
        user.setEmail("user@test.com");

        lenient().when(authentication.getName()).thenReturn("user@test.com");
    }

    @Test
    public void testViewNotifications() throws Exception {
        when(userService.findByEmail("user@test.com")).thenReturn(user);
        when(notificationService.getUserNotifications(1L)).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadCount(1L)).thenReturn(0L);

        mockMvc.perform(get("/notifications")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attribute("unreadCount", 0L));
    }

    @Test
    public void testGetUnreadCount() throws Exception {
        when(userService.findByEmail("user@test.com")).thenReturn(user);
        when(notificationService.getUnreadCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/notifications/unread/count")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    public void testMarkAsRead() throws Exception {
        mockMvc.perform(post("/notifications/100/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService, times(1)).markAsRead(100L);
    }
}
