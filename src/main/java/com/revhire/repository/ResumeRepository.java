package com.revhire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.revhire.model.Resume;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}