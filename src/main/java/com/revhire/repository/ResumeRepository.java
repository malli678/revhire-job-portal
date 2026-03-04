package com.revhire.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revhire.model.Resume;
import com.revhire.model.JobSeeker;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // ✅ CRITICAL METHOD ⭐⭐⭐
    Optional<Resume> findByJobSeeker(JobSeeker jobSeeker);
}