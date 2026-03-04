package com.revhire.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.revhire.model.JobSeeker;
import com.revhire.model.User.Role;
import com.revhire.service.*;
import com.revhire.repository.*;

@RunWith(MockitoJUnitRunner.class)
public class JobSeekerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobSeekerService jobSeekerService;

    @Mock
    private UserService userService;

    @Mock
    private JobService jobService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private JobSeekerRepository jobSeekerRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Principal principal;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private ResumeParserService resumeParserService;

    @Mock
    private EducationService educationService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ResumeService resumeService;

    @InjectMocks
    private JobSeekerController jobSeekerController;

    private JobSeeker jobSeeker;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobSeekerController).build();

        jobSeeker = new JobSeeker();
        jobSeeker.setUserId(1L);
        jobSeeker.setEmail("seeker@test.com");
        jobSeeker.setFullName("Test Seeker");
        jobSeeker.setRole(Role.JOBSEEKER);

        lenient().when(authentication.getName()).thenReturn("seeker@test.com");
        lenient().when(principal.getName()).thenReturn("seeker@test.com");
    }

    @Test
    public void testDashboard() throws Exception {
        when(userService.findByEmail("seeker@test.com")).thenReturn(jobSeeker);
        when(jobSeekerService.getSavedJobsList(1L)).thenReturn(new ArrayList<>());
        when(jobSeekerService.getApplicationsList(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/jobseeker/dashboard")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("jobseeker/dashboard"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("savedCount"))
                .andExpect(model().attributeExists("applicationCount"))
                .andExpect(model().attributeExists("completion"));
    }

    @Test
    public void testSearchJobs_NoSession() throws Exception {
        // Mocking missing session data which triggers UnauthorizedException
        try {
            mockMvc.perform(get("/jobseeker/search-jobs"));
        } catch (Exception e) {
            assertEquals("Session expired. Please login again.", e.getCause().getMessage());
        }
    }

    @Test
    public void testSearchJobs_Success() throws Exception {
        mockMvc.perform(get("/jobseeker/search-jobs")
                .sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("jobseeker/search-jobs"))
                .andExpect(model().attributeExists("jobs"))
                .andExpect(model().attribute("role", "JOBSEEKER"));
    }

    @Test
    public void testProfile_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(jobSeeker);
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));

        mockMvc.perform(get("/jobseeker/profile")
                .principal(authentication)
                .sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("jobseeker/profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void testAddSkill() throws Exception {
        when(jobSeekerService.getJobSeekerByEmail("seeker@test.com")).thenReturn(jobSeeker);

        mockMvc.perform(post("/jobseeker/profile/skill/add")
                .principal(authentication)
                .param("skillName", "Java"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jobseeker/profile"));

        verify(jobSeekerService, times(1)).addSkill(1L, "Java");
    }

    @Test
    public void testSaveJob() throws Exception {
        doReturn(ResponseEntity.ok("Job saved successfully")).when(jobSeekerService).saveJob(1L, 10L);

        mockMvc.perform(post("/jobseeker/saveJob/10")
                .sessionAttr("userId", 1L))
                .andExpect(status().isOk());

        verify(jobSeekerService, times(1)).saveJob(1L, 10L);
    }
}