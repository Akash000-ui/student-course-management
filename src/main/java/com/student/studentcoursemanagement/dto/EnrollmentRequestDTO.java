package com.student.studentcoursemanagement.dto;

public class EnrollmentRequestDTO {
    private String courseId;

    public EnrollmentRequestDTO() {}

    public EnrollmentRequestDTO(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return "EnrollmentRequestDTO{" +
                "courseId='" + courseId + '\'' +
                '}';
    }
}
