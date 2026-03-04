package com.revhire.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.revhire.service.ApplicationService;
import com.revhire.service.JobService;
import com.revhire.model.Job;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private JobService jobService;

    @InjectMocks
    private ApplicationController applicationController;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(applicationController).build();
    }

    @Test
    public void testApplyJob() throws Exception {
        mockMvc.perform(post("/applications/apply")
                .param("jobId", "10")
                .param("jobSeekerId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/applications/jobseeker/1"));

        verify(applicationService, times(1)).applyJob(10L, 1L);
    }

    @Test
    public void testAddNotes_Success() throws Exception {
        mockMvc.perform(post("/applications/add-notes")
                .param("applicationId", "100")
                .param("notes", "Looking good")
                .header("Referer", "http://localhost:8080/employer/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/employer/dashboard"));

        verify(applicationService, times(1)).addNotes(100L, "Looking good");
    }

    @Test
    public void testViewApplicationsByJobSeeker() throws Exception {
        when(applicationService.getApplicationsByJobSeeker(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/applications/jobseeker/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobseeker/applications"))
                .andExpect(model().attributeExists("applications"))
                .andExpect(model().attribute("jobSeekerId", 1L));
    }

    @Test
    public void testViewApplicationsByJob() throws Exception {
        when(applicationService.getApplicationsByJob(10L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/applications/job/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/applicants"))
                .andExpect(model().attributeExists("applications"))
                .andExpect(model().attribute("jobId", 10L));
    }

    @Test
    public void testShowApplyPage() throws Exception {
        Job job = new Job();
        job.setJobId(10L);
        when(jobService.getJobById(10L)).thenReturn(job);

        mockMvc.perform(get("/applications/apply/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobseeker/apply-job"))
                .andExpect(model().attribute("job", job));
    }
}
