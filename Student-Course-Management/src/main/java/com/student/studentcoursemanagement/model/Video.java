package com.student.studentcoursemanagement.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "videos")
public class Video {

    @Id
    private String id;

    private String title;
    private String description;
    private String courseId;
    private String videoUrl;
    private String driveNotesFileLink; // Google Drive link for notes
    private String driveNotesFileName; // Display name for notes file

    @Builder.Default
    private List<String> driveCodeFileLinks = new ArrayList<>(); // Google Drive links for code files

    @Builder.Default
    private List<String> driveCodeFileNames = new ArrayList<>(); // Display names for code files

    // Ordering position within a course (1-based). Nullable for legacy data.
    private Integer position;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
