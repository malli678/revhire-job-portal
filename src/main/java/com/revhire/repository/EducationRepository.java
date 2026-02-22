package com.revhire.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.revhire.model.Education;
import com.revhire.model.JobSeeker;

public interface EducationRepository extends JpaRepository<Education, Long> {

	List<Education> findByJobSeeker(JobSeeker jobSeeker);
	}