package com.revhire.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.revhire.model.Education;
import com.revhire.model.User;

public interface EducationRepository extends JpaRepository<Education, Long> {

    List<Education> findByUser(User user);
}