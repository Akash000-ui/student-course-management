package com.student.studentcoursemanagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String title;
    private String description;
    private String thumbnailUrl;
    private String categoryId; // Changed from CourseCategory enum to String ID
    private DifficultyLevel difficulty;

    @Builder.Default
    private List<String> videoIds = new ArrayList<>();

    // Trainer Information
    private String trainerName;
    private String trainerBio;
    private String experience;
    private String linkedinProfile;
    private String fieldOfWork;
    private String profilePictureUrl;
    private Language language;

    // Payment Fields
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CourseType courseType = CourseType.FREE;
    private Integer price; // Price in rupees (only for PAID courses)

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
