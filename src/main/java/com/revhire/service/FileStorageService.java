package com.revhire.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final String UPLOAD_DIR = "uploads/";

    public String storeFile(MultipartFile file) {

        try {

            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // ✅ Validate size (2MB)
            if (file.getSize() > 2 * 1024 * 1024) {
                throw new RuntimeException("File exceeds 2MB limit");
            }

            // ✅ Validate type
            String originalName = file.getOriginalFilename();

            if (originalName == null ||
                !(originalName.endsWith(".pdf") || originalName.endsWith(".docx"))) {

                throw new RuntimeException("Only PDF/DOCX files allowed");
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // ✅ UNIQUE FILE NAME ⭐⭐⭐⭐⭐
            String uniqueFileName = UUID.randomUUID() + "_" + originalName;

            Path filePath = uploadPath.resolve(uniqueFileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return uniqueFileName;   // ✅ store ONLY filename in DB

        } catch (IOException e) {
            throw new RuntimeException("File storage failed: " + e.getMessage());
        }
    }
}