package com.revhire.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.revhire.model.*;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByJobSeeker(JobSeeker jobSeeker);
}