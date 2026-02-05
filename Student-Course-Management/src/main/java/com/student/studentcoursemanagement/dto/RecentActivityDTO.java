package com.student.studentcoursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {
    private int newUsersToday;
    private int newEnrollmentsToday;
    private int activeUsersToday;
    private String mostPopularCourse;
    private int mostPopularCourseEnrollments;
}
