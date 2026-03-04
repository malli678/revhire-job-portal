package com.revhire.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revhire.model.Job;
import com.revhire.model.Employer;
import com.revhire.repository.JobRepository;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobAlertService jobAlertService;

    @InjectMocks
    private JobService jobService;

    private Job job;
    private Employer employer;

    @Before
    public void setUp() {
        employer = new Employer();
        employer.setUserId(2L);
        employer.setCompanyName("RevHire Inc");

        job = new Job();
        job.setJobId(1L);
        job.setTitle("Software Engineer");
        job.setDescription("Great job");
        job.setEmployer(employer);
        job.setLocation("Remote");
        job.setNumberOfOpenings(5);
        job.setStatus("ACTIVE");
    }

    @Test
    public void testGetJobById() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        Job foundJob = jobService.getJobById(1L);

        assertNotNull(foundJob);
        assertEquals("Software Engineer", foundJob.getTitle());
        verify(jobRepository, times(1)).findById(1L);
    }

    @Test(expected = RuntimeException.class)
    public void testGetJobById_NotFound() {
        when(jobRepository.findById(2L)).thenReturn(Optional.empty());

        jobService.getJobById(2L);
    }

    @Test
    public void testSaveJob() {
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        Job savedJob = jobService.saveJob(job, employer);

        assertNotNull(savedJob);
        assertEquals(employer, savedJob.getEmployer());
        verify(jobRepository, times(1)).save(job);
        verify(jobAlertService, times(1)).checkNewJobAgainstAlerts(job);
    }

    @Test
    public void testDeleteJob() {
        doNothing().when(jobRepository).deleteById(1L);

        jobService.deleteJob(1L);

        verify(jobRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testGetAllJobs() {
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        when(jobRepository.findAll()).thenReturn(jobs);

        List<Job> result = jobService.getAllJobs();

        assertEquals(1, result.size());
        assertEquals("Software Engineer", result.get(0).getTitle());
    }

    @Test
    public void testGetJobsByEmployer() {
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        when(jobRepository.findByEmployer(employer)).thenReturn(jobs);

        List<Job> result = jobService.getJobsByEmployer(employer);

        assertEquals(1, result.size());
        verify(jobRepository, times(1)).findByEmployer(employer);
    }
}
