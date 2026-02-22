package com.revhire.repository;

import com.revhire.model.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobSeekerRepository extends JpaRepository<JobSeeker, Long> {
    Optional<JobSeeker> findByEmail(String email);
    
    @Query("SELECT js FROM JobSeeker js WHERE :skill MEMBER OF js.skills")
    List<JobSeeker> findBySkill(@Param("skill") String skill);
    
    @Query("SELECT js FROM JobSeeker js WHERE js.totalExperienceYears >= :minExperience")
    List<JobSeeker> findByMinExperience(@Param("minExperience") Integer minExperience);
}