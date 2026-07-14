package com.student.studentcoursemanagement.exception;

public class EnrollmentNotFoundException extends RuntimeException {
    public EnrollmentNotFoundException(String message) {
        super(message);
    }
    
    public EnrollmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
