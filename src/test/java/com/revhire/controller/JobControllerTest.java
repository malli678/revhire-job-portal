package com.revhire.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.revhire.model.Job;
import com.revhire.service.EmployerService;
import com.revhire.service.JobService;

@RunWith(MockitoJUnitRunner.class)
public class JobControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobService jobService;

    @Mock
    private EmployerService employerService;

    @InjectMocks
    private JobController jobController;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController).build();
    }

    @Test
    public void testViewJobDetails() throws Exception {
        Job job1 = new Job();
        job1.setJobId(1L);
        job1.setTitle("Dev");

        when(jobService.getJobById(1L)).thenReturn(job1);

        mockMvc.perform(get("/jobs/view/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobseeker/job-details"))
                .andExpect(model().attributeExists("job"))
                .andExpect(model().attribute("job", job1));
    }
}
