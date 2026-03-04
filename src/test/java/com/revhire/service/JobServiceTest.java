package com.revhire.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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

    @InjectMocks
    private JobService jobService;

    private Job job;

    @Before
    public void setUp() {
        job = new Job();
        job.setJobId(1L);
        job.setTitle("Software Engineer");
        job.setDescription("Great job");
        Employer emp = new Employer();
        emp.setCompanyName("RevHire Inc");
        job.setEmployer(emp);
        job.setLocation("Remote");
        job.setNumberOfOpenings(5);
        job.setCreatedAt(LocalDateTime.now());
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

        jobService.getJobById(2L); // Should throw exception
    }
}
