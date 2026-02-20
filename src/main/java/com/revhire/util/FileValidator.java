package com.revhire.util;

import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB

    public static void validateResume(MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("File size exceeds 2MB limit");
        }

        String type = file.getContentType();

        if (!(type.equals("application/pdf") ||
              type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {

            throw new RuntimeException("Only PDF or DOCX allowed");
        }
    }
}