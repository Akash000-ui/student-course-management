package com.student.studentcoursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    private int totalUsers;
    private int totalCourses;
    private int totalEnrollments;
    private int totalVideos;
    private int newUsersThisMonth;
    private int newEnrollmentsThisMonth;
    private List<CourseStatsDTO> courseStats;
    private RecentActivityDTO recentActivity;
}
