package com.revhire.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
 
 private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

 @ExceptionHandler(ResourceNotFoundException.class)
 public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
     log.warn("Resource not found: {}", ex.getMessage());
     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
 }

 @ExceptionHandler(FileStorageException.class)
 public ResponseEntity<?> handleFileStorage(FileStorageException ex) {
     log.error("File storage error: {}", ex.getMessage(), ex);
     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
 }

 @ExceptionHandler(Exception.class)
 public ResponseEntity<?> handleGeneric(Exception ex) {
     log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
         .body("Something went wrong: " + ex.getMessage());
 }
}