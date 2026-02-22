package com.revhire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revhire.model.SavedJob;
import com.revhire.model.JobSeeker;
import com.revhire.model.Job;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByJobSeeker(JobSeeker jobSeeker);

    Optional<SavedJob> findByJobSeekerAndJob(JobSeeker jobSeeker, Job job);
}