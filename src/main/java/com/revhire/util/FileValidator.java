package com.revhire.util;

import org.springframework.web.multipart.MultipartFile;

import com.revhire.exception.FileStorageException;
public class FileValidator {

    private static final long MAX_RESUME_SIZE = 2 * 1024 * 1024; // 2MB

    public static void validateResume(MultipartFile file) {

        if (file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        if (file.getSize() > MAX_RESUME_SIZE) {
            throw new FileStorageException("File exceeds 2MB limit");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("application/pdf")
                 && !contentType.contains("word"))) {

            throw new FileStorageException("Only PDF/DOC/DOCX allowed");
        }
    }
}