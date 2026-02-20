package com.revhire.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.revhire.exception.ResourceNotFoundException;
import com.revhire.model.*;
import com.revhire.repository.*;

@Service
public class EducationService {

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ Add Education
    public ResponseEntity<?> addEducation(Long userId, Education education) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        education.setUser(user);

        educationRepository.save(education);

        return ResponseEntity.ok("Education added successfully");
    }

    // ✅ View Education
    public ResponseEntity<?> getEducation(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Education> educationList = educationRepository.findByUser(user);

        return ResponseEntity.ok(educationList);
    }

    // ✅ Delete Education
    public ResponseEntity<?> deleteEducation(Long educationId) {

        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found"));

        educationRepository.delete(education);

        return ResponseEntity.ok("Education removed successfully");
    }
}