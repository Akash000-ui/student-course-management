package com.student.studentcoursemanagement.exception;

public class AlreadyEnrolledException extends RuntimeException {
    public AlreadyEnrolledException(String message) {
        super(message);
    }
    
    public AlreadyEnrolledException(String message, Throwable cause) {
        super(message, cause);
    }
}
