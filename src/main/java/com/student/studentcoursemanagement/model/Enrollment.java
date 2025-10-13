package com.student.studentcoursemanagement.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "enrollments")
@CompoundIndex(def = "{'userId': 1, 'courseId': 1}", unique = true)
public class Enrollment {
    
    @Id
    private String id;
    
    private String userId;
    private String courseId;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastAccessedAt;
    private boolean isCompleted;
    private LocalDateTime completedAt;
    private int progressPercentage; // 0-100
    private int completedVideos;
    private int totalVideos;

    // Constructors
    public Enrollment() {
        this.enrolledAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        this.isCompleted = false;
        this.progressPercentage = 0;
        this.completedVideos = 0;
        this.totalVideos = 0;
    }

    public Enrollment(String userId, String courseId) {
        this();
        this.userId = userId;
        this.courseId = courseId;
    }

    // Getters and Setters
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
        if (completed && completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
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
        this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
        
        // Auto-complete if progress reaches 100%
        if (this.progressPercentage == 100 && !this.isCompleted) {
            this.setCompleted(true);
        }
    }

    public int getCompletedVideos() {
        return completedVideos;
    }

    public void setCompletedVideos(int completedVideos) {
        this.completedVideos = Math.max(0, completedVideos);
        updateProgress();
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = Math.max(0, totalVideos);
        updateProgress();
    }

    // Helper method to update progress percentage
    private void updateProgress() {
        if (totalVideos > 0) {
            this.progressPercentage = (completedVideos * 100) / totalVideos;
            
            // Auto-complete if all videos are completed
            if (completedVideos >= totalVideos && !this.isCompleted) {
                this.setCompleted(true);
            }
        }
    }

    // Method to mark a video as completed
    public void completeVideo() {
        this.completedVideos++;
        this.lastAccessedAt = LocalDateTime.now();
        updateProgress();
    }

    // Method to access the course (update last accessed time)
    public void accessCourse() {
        this.lastAccessedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", enrolledAt=" + enrolledAt +
                ", isCompleted=" + isCompleted +
                ", progressPercentage=" + progressPercentage +
                ", completedVideos=" + completedVideos +
                ", totalVideos=" + totalVideos +
                '}';
    }
}
