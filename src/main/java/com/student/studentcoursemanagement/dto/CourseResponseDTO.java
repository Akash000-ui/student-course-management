package com.student.studentcoursemanagement.dto;

import com.student.studentcoursemanagement.model.Course;
import com.student.studentcoursemanagement.model.CourseCategory;
import com.student.studentcoursemanagement.model.DifficultyLevel;
import com.student.studentcoursemanagement.model.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {

    private String id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private CourseCategory category;
    private DifficultyLevel difficulty;
    private List<String> videoIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Trainer Information
    private String trainerName;
    private String trainerBio;
    private String experience;
    private String linkedinProfile;
    private String fieldOfWork;
    private String profilePictureUrl;
    private Language language;

    public static CourseResponseDTO fromEntity(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .videoIds(course.getVideoIds())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .trainerName(course.getTrainerName())
                .trainerBio(course.getTrainerBio())
                .experience(course.getExperience())
                .linkedinProfile(course.getLinkedinProfile())
                .fieldOfWork(course.getFieldOfWork())
                .profilePictureUrl(course.getProfilePictureUrl())
                .language(course.getLanguage())
                .build();
    }
}
