package com.student.studentcoursemanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponseDTO {

    private String id;
    private String title;
    private String description;
    private String courseId;
    private String videoUrl;
    private String driveNotesFileLink; // Google Drive link for notes
    private String driveNotesFileName; // Display name for notes file
    private List<String> driveCodeFileLinks; // Google Drive links for code files
    private List<String> driveCodeFileNames; // Display names for code files
    private Integer position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
