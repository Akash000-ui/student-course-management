package com.student.studentcoursemanagement.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    private String description;

    @NotBlank(message = "Course ID is required")
    private String courseId;

    @Pattern(regexp = "^https?://(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[a-zA-Z0-9_-]{11}.*$", message = "Video URL must be a valid YouTube URL")
    private String videoUrl;

    private String driveNotesFileLink;

    private String driveNotesFileName;

    private List<String> driveCodeFileLinks;

    private List<String> driveCodeFileNames;

    private Integer position;
}
