package com.revhire.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.revhire.model.Job;
import com.revhire.model.Employer;
import com.revhire.dto.JobDto;
import com.revhire.service.EmployerService;
import com.revhire.service.JobService;

@RunWith(MockitoJUnitRunner.class)
public class JobControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobService jobService;

    @Mock
    private EmployerService employerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JobController jobController;

    private Job job;
    private Employer employer;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController).build();

        employer = new Employer();
        employer.setUserId(2L);
        employer.setEmail("employer@test.com");

        job = new Job();
        job.setJobId(10L);
        job.setTitle("Java Developer");

        lenient().when(authentication.getName()).thenReturn("employer@test.com");
    }

    @Test
    public void testViewJobDetails() throws Exception {
        when(jobService.getJobById(10L)).thenReturn(job);

        mockMvc.perform(get("/jobs/view/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobseeker/job-details"))
                .andExpect(model().attributeExists("job"));
    }

    @Test
    public void testSaveJob_Success() throws Exception {
        when(employerService.getEmployerByEmail("employer@test.com")).thenReturn(employer);

        mockMvc.perform(post("/jobs/save")
                .principal(authentication)
                .param("title", "Frontend Dev")
                .param("description", "React expert")
                .param("location", "Home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/manage-jobs"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(jobService, times(1)).saveJob(any(Job.class), eq(employer));
    }

    @Test
    public void testGetAllJobs() throws Exception {
        when(jobService.getAllJobs()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/jobs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testSearchJobsPage() throws Exception {
        when(jobService.advancedSearch(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/jobs/search-page")
                .param("title", "Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobseeker/search-jobs"))
                .andExpect(model().attributeExists("jobs"));
    }
}
