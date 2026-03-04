package com.revhire.repository;

import com.revhire.model.JobAlert;
import com.revhire.model.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobAlertRepository extends JpaRepository<JobAlert, Long> {
    
    List<JobAlert> findByJobSeeker(JobSeeker jobSeeker);
    
    List<JobAlert> findByJobSeekerAndIsActiveTrue(JobSeeker jobSeeker);
    
    List<JobAlert> findByIsActiveTrueAndFrequency(String frequency);
    
    @Query("SELECT ja FROM JobAlert ja WHERE ja.isActive = true")
    List<JobAlert> findAllActiveAlerts();
}