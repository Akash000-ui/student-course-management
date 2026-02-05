package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.dto.AdminDashboardStatsDTO;
import com.student.studentcoursemanagement.dto.CourseStatsDTO;
import com.student.studentcoursemanagement.dto.RecentActivityDTO;
import com.student.studentcoursemanagement.model.Course;
import com.student.studentcoursemanagement.model.Enrollment;
import com.student.studentcoursemanagement.model.UserVideoCompletion;
import com.student.studentcoursemanagement.model.Video;
import com.student.studentcoursemanagement.repo.CourseRepo;
import com.student.studentcoursemanagement.repo.EnrollmentRepo;
import com.student.studentcoursemanagement.repo.UserRepo;
import com.student.studentcoursemanagement.repo.VideoRepo;
import com.student.studentcoursemanagement.repo.UserVideoCompletionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminStatsService {

    private static final Logger logger = LoggerFactory.getLogger(AdminStatsService.class);

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private CourseRepo courseRepository;

    @Autowired
    private EnrollmentRepo enrollmentRepository;

    @Autowired
    private VideoRepo videoRepository;

    @Autowired
    private UserVideoCompletionRepo userVideoCompletionRepository;

    public AdminDashboardStatsDTO getAdminDashboardStats() {
        logger.info("Fetching admin dashboard statistics");

        // Calculate total counts
        int totalUsers = (int) userRepository.count();
        int totalCourses = (int) courseRepository.count();
        int totalEnrollments = (int) enrollmentRepository.count();
        int totalVideos = (int) videoRepository.count();

        // Calculate new users this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        int newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
        int newEnrollmentsThisMonth = enrollmentRepository.countByEnrolledAtAfter(startOfMonth);

        // Get course statistics
        List<CourseStatsDTO> courseStats = getCourseStatistics();

        // Get recent activity
        RecentActivityDTO recentActivity = getRecentActivity();

        AdminDashboardStatsDTO stats = AdminDashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalCourses(totalCourses)
                .totalEnrollments(totalEnrollments)
                .totalVideos(totalVideos)
                .newUsersThisMonth(newUsersThisMonth)
                .newEnrollmentsThisMonth(newEnrollmentsThisMonth)
                .courseStats(courseStats)
                .recentActivity(recentActivity)
                .build();

        logger.info("Admin dashboard statistics fetched successfully");
        return stats;
    }

    /**
     * Sync course videoIds with actual videos in database
     * This fixes any courses that have empty videoIds list
     */
    private void syncCourseVideoIds(List<Course> courses) {
        for (Course course : courses) {
            if (course.getVideoIds() == null || course.getVideoIds().isEmpty()) {
                logger.warn("Course '{}' has empty videoIds. Syncing with actual videos...", course.getTitle());
                
                // Get all videos for this course from the video repository
                List<Video> videos = videoRepository.findByCourseId(course.getId());
                List<String> videoIds = videos.stream()
                        .map(Video::getId)
                        .collect(Collectors.toList());
                
                if (!videoIds.isEmpty()) {
                    course.setVideoIds(videoIds);
                    courseRepository.save(course);
                    logger.info("Synced {} videos to course '{}'", videoIds.size(), course.getTitle());
                }
            }
        }
    }

    private List<CourseStatsDTO> getCourseStatistics() {
        List<Course> courses = courseRepository.findAll();
        List<CourseStatsDTO> courseStatsList = new ArrayList<>();

        // Sync course videoIds if they are empty (one-time fix for existing data)
        syncCourseVideoIds(courses);

        for (Course course : courses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());

            int totalEnrollments = enrollments.size();
            long totalVideosInCourse = videoRepository.countByCourseId(course.getId());

            logger.info("Course: {}, Total Enrollments: {}, Total Videos: {}",
                    course.getTitle(), totalEnrollments, totalVideosInCourse);

            // Calculate active and completed enrollments based on video completions
            int activeEnrollments = 0;
            int completedEnrollments = 0;

            for (Enrollment enrollment : enrollments) {
                // Get all video completions for this user in this course
                List<UserVideoCompletion> userCompletions = userVideoCompletionRepository
                        .findByUserIdAndCourseId(enrollment.getUserId(), course.getId());

                logger.info("  User: {}, Total completions found: {}, Course videoIds count: {}",
                        enrollment.getUserId(), userCompletions.size(),
                        course.getVideoIds() != null ? course.getVideoIds().size() : 0);

                // Count only completions for videos that still exist in the course
                // NOTE: If course.videoIds is empty, we'll count all completions
                long completedVideosCount;
                if (course.getVideoIds() != null && !course.getVideoIds().isEmpty()) {
                    completedVideosCount = userCompletions.stream()
                            .filter(completion -> course.getVideoIds().contains(completion.getVideoId()))
                            .count();
                    logger.info("    Filtered completions (by videoIds): {}", completedVideosCount);
                } else {
                    // If videoIds is not maintained, count all completions for this course
                    completedVideosCount = userCompletions.size();
                    logger.info("    Using all completions (videoIds empty): {}", completedVideosCount);
                }

                // Calculate progress percentage
                double progress = totalVideosInCourse > 0
                        ? (completedVideosCount * 100.0) / totalVideosInCourse
                        : 0.0;

                logger.info("  User: {}, Completed: {}/{}, Progress: {}%",
                        enrollment.getUserId(), completedVideosCount,
                        totalVideosInCourse, String.format("%.2f", progress));

                // Categorize enrollment
                if (totalVideosInCourse > 0 && completedVideosCount >= totalVideosInCourse) {
                    completedEnrollments++;
                    progress = 100.0;
                    logger.info("    -> Counted as COMPLETED");
                } else if (completedVideosCount > 0) {
                    activeEnrollments++;
                    logger.info("    -> Counted as ACTIVE");
                } else {
                    logger.info("    -> Not counted (no progress)");
                }
                // If progress is 0, enrollment is neither active nor completed (just enrolled)
            }

            logger.info("Course: {} - Active: {}, Completed: {}",
                    course.getTitle(), activeEnrollments, completedEnrollments);

            CourseStatsDTO stats = CourseStatsDTO.builder()
                    .courseId(course.getId())
                    .courseTitle(course.getTitle())
                    .totalEnrollments(totalEnrollments)
                    .activeEnrollments(activeEnrollments)
                    .completedEnrollments(completedEnrollments)
                    .totalVideos((int) totalVideosInCourse)
                    .build();

            courseStatsList.add(stats);
        }

        return courseStatsList;
    }

    private RecentActivityDTO getRecentActivity() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        int newUsersToday = userRepository.countByCreatedAtAfter(startOfDay);
        int newEnrollmentsToday = enrollmentRepository.countByEnrolledAtAfter(startOfDay);

        // For active users today, we'll use a simple count of all users
        // In a real app, you'd track last login time
        int activeUsersToday = newUsersToday;

        // Find most popular course
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        Map<String, Long> courseEnrollmentCounts = allEnrollments.stream()
                .collect(Collectors.groupingBy(Enrollment::getCourseId, Collectors.counting()));

        String mostPopularCourseId = null;
        long maxEnrollments = 0;

        for (Map.Entry<String, Long> entry : courseEnrollmentCounts.entrySet()) {
            if (entry.getValue() > maxEnrollments) {
                maxEnrollments = entry.getValue();
                mostPopularCourseId = entry.getKey();
            }
        }

        String mostPopularCourse = "N/A";
        if (mostPopularCourseId != null) {
            Course course = courseRepository.findById(mostPopularCourseId).orElse(null);
            if (course != null) {
                mostPopularCourse = course.getTitle();
            }
        }

        return RecentActivityDTO.builder()
                .newUsersToday(newUsersToday)
                .newEnrollmentsToday(newEnrollmentsToday)
                .activeUsersToday(activeUsersToday)
                .mostPopularCourse(mostPopularCourse)
                .mostPopularCourseEnrollments((int) maxEnrollments)
                .build();
    }
}
