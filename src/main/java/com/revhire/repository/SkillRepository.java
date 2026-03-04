
package com.revhire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.revhire.model.Skill;
import com.revhire.model.JobSeeker;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    // ✅ Get all skills of a JobSeeker
    List<Skill> findByJobSeeker(JobSeeker jobSeeker);
}