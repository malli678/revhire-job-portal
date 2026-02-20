package com.revhire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.revhire.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

}