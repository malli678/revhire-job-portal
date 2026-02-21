package com.revhire.repository;

import com.revhire.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByLocation(String location);

    List<Job> findByJobType(String jobType);

    List<Job> findByTitleContaining(String title);

    List<Job> findByStatus(String status);
}