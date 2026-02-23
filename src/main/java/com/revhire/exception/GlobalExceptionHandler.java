package com.revhire.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    // ✅ File Storage Errors
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<?> handleFileStorage(FileStorageException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    // ✅ Unauthorized Access
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    // ✅ Catch All (Safety Net)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + ex.getMessage());
    }
}