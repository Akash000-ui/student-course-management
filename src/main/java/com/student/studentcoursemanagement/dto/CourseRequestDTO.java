package com.student.studentcoursemanagement.dto;

import com.student.studentcoursemanagement.model.CourseCategory;
import com.student.studentcoursemanagement.model.DifficultyLevel;
import com.student.studentcoursemanagement.model.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private CourseCategory category;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficulty;

    private String thumbnailUrl;

    // Trainer Information
    @NotBlank(message = "Trainer name is required")
    @Size(min = 2, max = 50, message = "Trainer name must be between 2 and 50 characters")
    private String trainerName;

    @NotBlank(message = "Trainer bio is required")
    @Size(min = 10, message = "Trainer bio must be at least 10 characters")
    private String trainerBio;

    @NotBlank(message = "Experience is required")
    private String experience;

    private String linkedinProfile;

    @NotBlank(message = "Field of work is required")
    private String fieldOfWork;

    private String profilePictureUrl;

    @NotNull(message = "Language is required")
    private Language language;
}
