package com.revhire.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.model.Application;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.EmployerRepository;
import com.revhire.repository.JobRepository;

@RunWith(MockitoJUnitRunner.class)
public class EmployerServiceTest {

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private EmployerService employerService;

    private Employer employer;
    private Job job;

    @Before
    public void setUp() {
        employer = new Employer();
        employer.setUserId(1L);
        employer.setCompanyName("Tech Corp");

        job = new Job();
        job.setJobId(10L);
        job.setStatus("ACTIVE");
    }

    @Test
    public void testGetEmployerById() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(employer));

        Employer found = employerService.getEmployerById(1L);

        assertEquals("Tech Corp", found.getCompanyName());
        verify(employerRepository, times(1)).findById(1L);
    }

    @Test
    public void testCountTotalJobs() {
        when(jobRepository.countByEmployerUserId(1L)).thenReturn(5L);

        long count = employerService.countTotalJobs(employer);

        assertEquals(5L, count);
        verify(jobRepository, times(1)).countByEmployerUserId(1L);
    }

    @Test
    public void testCountActiveJobs() {
        when(jobRepository.countByEmployerUserIdAndStatus(1L, "ACTIVE")).thenReturn(3L);

        long count = employerService.countActiveJobs(employer);

        assertEquals(3L, count);
        verify(jobRepository, times(1)).countByEmployerUserIdAndStatus(1L, "ACTIVE");
    }

    @Test
    public void testCountPendingReviews() {
        when(applicationRepository.countByJob_Employer_UserIdAndStatus(1L, Application.ApplicationStatus.APPLIED))
                .thenReturn(10L);
        when(applicationRepository.countByJob_Employer_UserIdAndStatus(1L, Application.ApplicationStatus.UNDER_REVIEW))
                .thenReturn(5L);

        long count = employerService.countPendingReviews(employer);

        assertEquals(15L, count);
    }

    @Test
    public void testCloseJob() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

        employerService.closeJob(10L);

        assertEquals("CLOSED", job.getStatus());
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    public void testReopenJob() {
        job.setStatus("CLOSED");
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

        employerService.reopenJob(10L);

        assertEquals("ACTIVE", job.getStatus());
        verify(jobRepository, times(1)).save(job);
    }
}
