package com.student.studentcoursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatsDTO {
    private String courseId;
    private String courseTitle;
    private int totalEnrollments;
    private int activeEnrollments;
    private int completedEnrollments;
    private int totalVideos;
}
