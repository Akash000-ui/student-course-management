package com.student.studentcoursemanagement.dto;

import java.time.LocalDateTime;

public class EnrollmentResponseDTO {
    private String id;
    private String userId;
    private String courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseThumbnail;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastAccessedAt;
    private boolean isCompleted;
    private LocalDateTime completedAt;
    private int progressPercentage;
    private int completedVideos;
    private int totalVideos;

    // Constructors
    public EnrollmentResponseDTO() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public String getCourseThumbnail() {
        return courseThumbnail;
    }

    public void setCourseThumbnail(String courseThumbnail) {
        this.courseThumbnail = courseThumbnail;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public int getCompletedVideos() {
        return completedVideos;
    }

    public void setCompletedVideos(int completedVideos) {
        this.completedVideos = completedVideos;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = totalVideos;
    }

    @Override
    public String toString() {
        return "EnrollmentResponseDTO{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", courseTitle='" + courseTitle + '\'' +
                ", enrolledAt=" + enrolledAt +
                ", isCompleted=" + isCompleted +
                ", progressPercentage=" + progressPercentage +
                ", completedVideos=" + completedVideos +
                ", totalVideos=" + totalVideos +
                '}';
    }
}
