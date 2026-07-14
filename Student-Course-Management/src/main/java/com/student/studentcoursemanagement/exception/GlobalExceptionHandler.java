package com.student.studentcoursemanagement.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.student.studentcoursemanagement.dto.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCourseNotFoundException(CourseNotFoundException ex) {
        logger.error("Course not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        response.setStatusCode(404);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidCourseDataException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCourseDataException(InvalidCourseDataException ex) {
        logger.error("Invalid course data: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        response.setStatusCode(400);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleVideoNotFoundException(VideoNotFoundException ex) {
        logger.error("Video not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        response.setStatusCode(404);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidVideoDataException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidVideoDataException(InvalidVideoDataException ex) {
        logger.error("Invalid video data: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        response.setStatusCode(400);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AlreadyEnrolledException.class)
    public ResponseEntity<ApiResponse<Object>> handleAlreadyEnrolledException(AlreadyEnrolledException ex) {
        logger.error("Already enrolled: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        response.setStatusCode(409);
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EnrollmentNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEnrollmentNotFoundException(EnrollmentNotFoundException ex) {
        logger.error("Enrollment not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        response.setStatusCode(404);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("Validation error: {}", ex.getMessage());
        
        BindingResult result = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        
        for (FieldError error : result.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        
        ApiResponse<Object> response = new ApiResponse<>(false, "Validation failed", errors);
        response.setStatusCode(400);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = new ApiResponse<>(false, "An unexpected error occurred", null);
        response.setStatusCode(500);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
