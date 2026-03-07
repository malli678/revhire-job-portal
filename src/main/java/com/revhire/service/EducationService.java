package com.revhire.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.revhire.exception.ResourceNotFoundException;
import com.revhire.model.Education;
import com.revhire.model.JobSeeker;
import com.revhire.repository.EducationRepository;
import com.revhire.repository.JobSeekerRepository;

@Service
public class EducationService {

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private JobSeekerRepository jobSeekerRepository;

    //  Add Education
    public ResponseEntity<?> addEducation(Long jobSeekerId, Education education) {

        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        education.setJobSeeker(jobSeeker);

        educationRepository.save(education);

        return ResponseEntity.ok("Education added successfully");
    }

    //  View Education
    public ResponseEntity<?> getEducation(Long jobSeekerId) {

        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        List<Education> educationList = educationRepository.findByJobSeeker(jobSeeker);

        return ResponseEntity.ok(educationList);
    }

    //  Delete Education
    public ResponseEntity<?> deleteEducation(Long educationId) {

        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found"));

        educationRepository.delete(education);

        return ResponseEntity.ok("Education removed successfully");
    }
}