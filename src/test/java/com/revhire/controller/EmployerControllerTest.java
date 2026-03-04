package com.revhire.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.revhire.model.Employer;
import com.revhire.model.User.Role;
import com.revhire.repository.ApplicationRepository;
import com.revhire.service.ApplicationService;
import com.revhire.service.EmployerService;
import com.revhire.service.JobService;
import com.revhire.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class EmployerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployerService employerService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private JobService jobService;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EmployerController employerController;

    private Employer employer;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employerController).build();

        employer = new Employer();
        employer.setUserId(1L);
        employer.setEmail("test@employer.com");
        employer.setCompanyName("Test Corp");
        employer.setRole(Role.EMPLOYER);
    }

    @Test
    public void testManageJobs() throws Exception {
        when(authentication.getName()).thenReturn("test@employer.com");
        when(employerService.getEmployerByEmail("test@employer.com")).thenReturn(employer);
        when(jobService.getJobsByEmployer(employer)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/employer/manage-jobs").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/manage-jobs"))
                .andExpect(model().attributeExists("jobs"));
    }

    @Test
    public void testCompanyProfile() throws Exception {
        when(authentication.getName()).thenReturn("test@employer.com");
        when(employerService.getEmployerByEmail("test@employer.com")).thenReturn(employer);

        mockMvc.perform(get("/employer/company-profile").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/company-profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void testCloseJob() throws Exception {
        mockMvc.perform(post("/employer/job/close/10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/manage-jobs"));

        verify(jobService).closeJob(10L);
    }

    @Test
    public void testReopenJob() throws Exception {
        mockMvc.perform(post("/employer/job/reopen/10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/manage-jobs"));

        verify(jobService).reopenJob(10L);
    }

    @Test
    public void testDeleteJob() throws Exception {
        mockMvc.perform(post("/employer/job/delete/10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/manage-jobs"));

        verify(jobService).deleteJob(10L);
    }

    @Test
    public void testMarkJobAsFilled() throws Exception {
        mockMvc.perform(post("/employer/job/filled/10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/manage-jobs"));

        verify(jobService).markFilled(10L);
    }
}
