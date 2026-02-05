package com.student.studentcoursemanagement.exception;

public class InvalidVideoDataException extends RuntimeException {
    
    public InvalidVideoDataException(String message) {
        super(message);
    }
    
    public InvalidVideoDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
