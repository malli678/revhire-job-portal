package com.revhire.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import com.revhire.model.JobSeeker;
import com.revhire.model.Job;
import com.revhire.model.Application;
import com.revhire.model.SavedJob;
import com.revhire.repository.JobSeekerRepository;
import com.revhire.repository.JobRepository;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.SavedJobRepository;
import com.revhire.repository.SkillRepository;

@RunWith(MockitoJUnitRunner.class)
public class JobSeekerServiceTest {

    @Mock
    private JobSeekerRepository jobSeekerRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private JobSeekerService jobSeekerService;

    private JobSeeker jobSeeker;
    private Job job;
    private Application application;

    @Before
    public void setUp() {
        jobSeeker = new JobSeeker();
        jobSeeker.setUserId(1L);
        jobSeeker.setEmail("seeker@test.com");
        jobSeeker.setFullName("Test Seeker");

        job = new Job();
        job.setJobId(10L);
        job.setTitle("Java Developer");

        application = new Application();
        application.setId(100L);
        application.setJobSeeker(jobSeeker);
        application.setJob(job);
        application.setStatus(Application.ApplicationStatus.APPLIED);
    }

    @Test
    public void testGetJobSeekerByEmail() {
        when(jobSeekerRepository.findByEmail("seeker@test.com")).thenReturn(Optional.of(jobSeeker));

        JobSeeker result = jobSeekerService.getJobSeekerByEmail("seeker@test.com");

        assertNotNull(result);
        assertEquals("Test Seeker", result.getFullName());
    }

    @Test
    public void testSaveJob_Success() {
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(savedJobRepository.findByJobSeekerAndJob(jobSeeker, job)).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobSeekerService.saveJob(1L, 10L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Job saved successfully", response.getBody());
        verify(savedJobRepository, times(1)).save(any(SavedJob.class));
    }

    @Test
    public void testWithdrawApplication_Success() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        jobSeekerService.withdrawApplication(100L, "Found another job");

        assertEquals(Application.ApplicationStatus.WITHDRAWN, application.getStatus());
        assertEquals("Found another job", application.getNotes());
        verify(applicationRepository, times(1)).save(application);
    }

    @Test(expected = RuntimeException.class)
    public void testWithdrawApplication_AlreadyRejected() {
        application.setStatus(Application.ApplicationStatus.REJECTED);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        jobSeekerService.withdrawApplication(100L, "Notes");
    }

    @Test
    public void testAddSkill() {
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));

        jobSeekerService.addSkill(1L, "Java");

        verify(skillRepository, times(1)).save(any());
    }
}
