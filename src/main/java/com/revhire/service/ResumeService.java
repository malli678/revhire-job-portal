package com.revhire.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.revhire.dto.ResumeDto;
import com.revhire.exception.FileStorageException;
import com.revhire.model.JobSeeker;
import com.revhire.model.Resume;
import com.revhire.model.User;
import com.revhire.repository.ResumeRepository;
import com.revhire.repository.UserRepository;
import com.revhire.util.FileValidator;
@Service
public class ResumeService {

    @Autowired
    private FileStorageService storageService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserRepository userRepository;

    public void uploadResume(MultipartFile file, String email) {

        try {
            FileValidator.validateResume(file);

            String path = storageService.storeFile(file);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            JobSeeker jobSeeker = (JobSeeker) user;

            Resume resume = resumeRepository.findByJobSeeker(jobSeeker)
                    .orElse(new Resume());

            resume.setFileName(file.getOriginalFilename());
            resume.setFileType(file.getContentType());
            resume.setFilePath(path);
            resume.setJobSeeker(jobSeeker);

            resumeRepository.save(resume);

            // ✅ IMPORTANT → update JobSeeker resumePath ⭐⭐⭐
            jobSeeker.setResumePath(path);
            userRepository.save(jobSeeker);

        } catch (Exception e) {
            throw new FileStorageException("Resume upload failed: " + e.getMessage());
        }
    }

    // ✅ SAVE BUILDER DATA ⭐⭐⭐
    public void save(ResumeDto dto, String email) {

        try {

            System.out.println("DTO Objective: " + dto.getObjective());
            System.out.println("DTO Degree: " + dto.getDegree());
            System.out.println("DTO Year: " + dto.getYear());
            System.out.println("DTO Skills: " + dto.getSkills());

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User NOT FOUND"));

            JobSeeker js = (JobSeeker) user;

            js.setObjective(dto.getObjective());
            js.setDegree(dto.getDegree());
            js.setYear(dto.getYear());

            if(dto.getSkills() != null && !dto.getSkills().isBlank()) {

                Set<String> skillSet = new HashSet<>();

                for(String skill : dto.getSkills().split(",")) {
                    if(!skill.isBlank()) {
                        skillSet.add(skill.trim());
                    }
                }

                js.setSkills(skillSet);
            }

            userRepository.save(js);

            System.out.println("✅ Resume Saved Successfully");

        } catch (Exception e) {

            System.out.println("❌ REAL ERROR:");
            e.printStackTrace();   // ⭐⭐⭐ MUST SEE THIS
            throw new RuntimeException(e);
        }
    }
}