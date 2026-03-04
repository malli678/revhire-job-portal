package com.revhire.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.revhire.dto.EmployerRegistrationDto;
import com.revhire.dto.JobSeekerRegistrationDto;
import com.revhire.service.UserService;
import com.revhire.model.JobSeeker;
import com.revhire.model.Employer;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testShowLoginForm() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("loginDto"));
    }

    @Test
    public void testShowJobSeekerRegistrationForm() throws Exception {
        mockMvc.perform(get("/auth/register/seeker"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-seeker"))
                .andExpect(model().attributeExists("registrationDto"));
    }

    @Test
    public void testJobSeekerRegistration_Success() throws Exception {
        when(userService.registerJobSeeker(any(JobSeekerRegistrationDto.class))).thenReturn(new JobSeeker());

        mockMvc.perform(post("/auth/register/seeker")
                .param("fullName", "John Doe")
                .param("email", "john@example.com")
                .param("password", "StrongPass@123")
                .param("confirmPassword", "StrongPass@123")
                .param("phoneNumber", "1234567890")
                .param("location", "NYC")
                .param("securityQuestion", "What is your pet's name?")
                .param("securityAnswer", "Fluffy")
                .param("currentEmploymentStatus", "EMPLOYED")
                .param("totalExperienceYears", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attributeExists("success"));

        verify(userService, times(1)).registerJobSeeker(any(JobSeekerRegistrationDto.class));
    }

    @Test
    public void testJobSeekerRegistration_PasswordMismatch() throws Exception {
        mockMvc.perform(post("/auth/register/seeker")
                .param("fullName", "John Doe")
                .param("email", "john@example.com")
                .param("password", "pass123")
                .param("confirmPassword", "wrongpass")
                .param("phoneNumber", "1234567890")
                .param("location", "NYC")
                .param("securityQuestion", "Q")
                .param("securityAnswer", "A")
                .param("currentEmploymentStatus", "EMPLOYED"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-seeker"))
                .andExpect(model().hasErrors());

        verify(userService, never()).registerJobSeeker(any(JobSeekerRegistrationDto.class));
    }

    @Test
    public void testShowEmployerRegistrationForm() throws Exception {
        mockMvc.perform(get("/auth/register/employer"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-employer"))
                .andExpect(model().attributeExists("registrationDto"));
    }

    @Test
    public void testEmployerRegistration_Success() throws Exception {
        when(userService.registerEmployer(any(EmployerRegistrationDto.class))).thenReturn(new Employer());

        mockMvc.perform(post("/auth/register/employer")
                .param("fullName", "Jane Doe")
                .param("email", "jane@company.com")
                .param("password", "StrongPass@123")
                .param("confirmPassword", "StrongPass@123")
                .param("companyName", "Tech Corp")
                .param("industry", "IT")
                .param("companySize", "51-200")
                .param("headquarters", "Seattle")
                .param("securityQuestion", "Q")
                .param("securityAnswer", "A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attributeExists("success"));

        verify(userService, times(1)).registerEmployer(any(EmployerRegistrationDto.class));
    }
}
