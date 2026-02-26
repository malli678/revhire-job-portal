package com.revhire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.revhire.model.Certification;
import com.revhire.model.JobSeeker;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    // ✅ Get certifications by JobSeeker
    List<Certification> findByJobSeeker(JobSeeker jobSeeker);
}