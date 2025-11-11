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
    private LocalDateTime completedAt; // Set only when course is 100% complete

    // REMOVED: progressPercentage, completedVideos, totalVideos, isCompleted
    // These are now calculated dynamically from UserVideoCompletion collection

    // Constructors
    public Enrollment() {
        this.enrolledAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
                ", lastAccessedAt=" + lastAccessedAt +
                ", completedAt=" + completedAt +
                '}';
    }
}
