package com.student.studentcoursemanagement.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "enrollments")
@CompoundIndex(def = "{'userId': 1, 'courseId': 1}", unique = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    private String id;

    private String userId;
    private String courseId;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime completedAt; // Set only when course is 100% complete

    // Payment & Enrollment Type
    @Builder.Default
    private EnrollmentType enrollmentType = EnrollmentType.FREE;
    private String orderId; // Foreign key to Order (optional, only for PAID enrollments)

    // REMOVED: progressPercentage, completedVideos, totalVideos, isCompleted
    // These are now calculated dynamically from UserVideoCompletion collection

    // Method to access the course (update last accessed time)
    public void accessCourse() {
        this.lastAccessedAt = LocalDateTime.now();
    }
}
