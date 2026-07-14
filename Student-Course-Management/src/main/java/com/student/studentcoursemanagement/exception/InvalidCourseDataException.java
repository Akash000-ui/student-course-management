package com.student.studentcoursemanagement.exception;

public class InvalidCourseDataException extends RuntimeException {
    
    public InvalidCourseDataException(String message) {
        super(message);
    }
    
    public InvalidCourseDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
