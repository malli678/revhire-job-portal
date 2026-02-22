package com.revhire.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.revhire.exception.FileStorageException;
import com.revhire.model.Resume;
import com.revhire.repository.ResumeRepository;
import com.revhire.util.FileValidator;

@Service
public class ResumeService {

    @Autowired
    private FileStorageService storageService;

    @Autowired
    private ResumeRepository resumeRepository;

    public ResponseEntity<?> uploadResume(MultipartFile file) {

        try {
            FileValidator.validateResume(file);

            String path = storageService.storeFile(file);

            Resume resume = new Resume();
            resume.setFileName(file.getOriginalFilename());
            resume.setFileType(file.getContentType());
            resume.setFilePath(path);

            Resume savedResume = resumeRepository.save(resume);

            return ResponseEntity.ok(savedResume);

        } catch (Exception e) {
            throw new FileStorageException("Resume upload failed: " + e.getMessage());
        }
    }
}