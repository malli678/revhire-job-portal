package com.revhire.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revhire.model.Application;
import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.model.JobSeeker;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.JobRepository;
import com.revhire.repository.JobSeekerRepository;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobSeekerRepository jobSeekerRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ApplicationService applicationService;

    private Job job;
    private JobSeeker jobSeeker;
    private Application application;
    private Employer employer;

    @Before
    public void setUp() {
        employer = new Employer();
        employer.setUserId(2L);

        job = new Job();
        job.setJobId(10L);
        job.setTitle("Java Developer");
        job.setEmployer(employer);
        job.setApplications(new ArrayList<>());

        jobSeeker = new JobSeeker();
        jobSeeker.setUserId(1L);
        jobSeeker.setFullName("Test Seeker");
        jobSeeker.setEmail("seeker@test.com");
        jobSeeker.setApplications(new ArrayList<>());

        application = new Application();
        application.setId(100L);
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setStatus(Application.ApplicationStatus.APPLIED);
    }

    @Test
    public void testApplyJob_Success() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(applicationRepository.findByJobAndJobSeeker(job, jobSeeker)).thenReturn(Optional.empty());

        applicationService.applyJob(10L, 1L);

        verify(applicationRepository, times(1)).save(any(Application.class));
        verify(notificationService, times(2)).createNotification(anyLong(), anyString(), anyString(), anyString());
    }

    @Test(expected = RuntimeException.class)
    public void testApplyJob_AlreadyApplied() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(applicationRepository.findByJobAndJobSeeker(job, jobSeeker)).thenReturn(Optional.of(application));

        applicationService.applyJob(10L, 1L);
    }

    @Test
    public void testWithdrawApplication_Success() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        applicationService.withdrawApplication(100L, "Withdrawing");

        assertEquals(Application.ApplicationStatus.WITHDRAWN, application.getStatus());
        verify(applicationRepository, times(1)).save(application);
        verify(notificationService, times(1)).createNotification(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    public void testShortlistCandidate() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        applicationService.shortlistCandidate(100L, "Good fit");

        assertEquals(Application.ApplicationStatus.SHORTLISTED, application.getStatus());
        verify(emailService, times(1)).sendApplicationStatusUpdate(any(), anyString());
        verify(notificationService, times(1)).createNotification(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    public void testRejectCandidate() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        applicationService.rejectCandidate(100L, "Not enough experience");

        assertEquals(Application.ApplicationStatus.REJECTED, application.getStatus());
        verify(emailService, times(1)).sendApplicationStatusUpdate(any(), anyString());
    }

    @Test
    public void testAddNotes() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        applicationService.addNotes(100L, "Checked references");

        assertEquals("Checked references", application.getNotes());
        verify(applicationRepository, times(1)).save(application);
    }
}
