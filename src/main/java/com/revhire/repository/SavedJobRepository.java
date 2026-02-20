package com.revhire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revhire.model.SavedJob;
import com.revhire.model.User;
import com.revhire.model.Job;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByUser(User user);

    Optional<SavedJob> findByUserAndJob(User user, Job job);
}