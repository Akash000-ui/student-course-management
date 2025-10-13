package com.student.studentcoursemanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.student.studentcoursemanagement.dto.EnrollmentRequestDTO;
import com.student.studentcoursemanagement.dto.EnrollmentResponseDTO;
import com.student.studentcoursemanagement.exception.AlreadyEnrolledException;
import com.student.studentcoursemanagement.exception.CourseNotFoundException;
import com.student.studentcoursemanagement.exception.EnrollmentNotFoundException;
import com.student.studentcoursemanagement.model.Course;
import com.student.studentcoursemanagement.model.Enrollment;
import com.student.studentcoursemanagement.repo.CourseRepo;
import com.student.studentcoursemanagement.repo.EnrollmentRepo;
import com.student.studentcoursemanagement.repo.VideoRepo;

@Service
public class EnrollmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private VideoRepo videoRepo;

    /**
     * Enroll user in a course
     */
    public EnrollmentResponseDTO enrollInCourse(String userId, EnrollmentRequestDTO request) {
        logger.info("Enrolling user {} in course {}", userId, request.getCourseId());

        // Check if course exists
        Course course = courseRepo.findById(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + request.getCourseId()));

        // Check if user is already enrolled
        if (enrollmentRepo.existsByUserIdAndCourseId(userId, request.getCourseId())) {
            throw new AlreadyEnrolledException("User is already enrolled in this course");
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment(userId, request.getCourseId());
        
        // Set total videos count
        int totalVideos = (int) videoRepo.countByCourseId(request.getCourseId());
        enrollment.setTotalVideos(totalVideos);

        // Save enrollment
        Enrollment savedEnrollment = enrollmentRepo.save(enrollment);
        
        logger.info("User {} successfully enrolled in course {}", userId, request.getCourseId());
        
        return mapToResponseDTO(savedEnrollment, course);
    }

    /**
     * Get all enrollments for a user
     */
    public List<EnrollmentResponseDTO> getUserEnrollments(String userId) {
        logger.info("Fetching enrollments for user {}", userId);

        List<Enrollment> enrollments = enrollmentRepo.findByUserIdOrderByEnrolledAtDesc(userId);
        
        return enrollments.stream()
                .map(this::mapToResponseDTOWithCourse)
                .collect(Collectors.toList());
    }

    /**
     * Get specific enrollment by user and course
     */
    public EnrollmentResponseDTO getEnrollment(String userId, String courseId) {
        logger.info("Fetching enrollment for user {} and course {}", userId, courseId);

        Enrollment enrollment = enrollmentRepo.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found for user and course"));

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));

        return mapToResponseDTO(enrollment, course);
    }

    /**
     * Update enrollment progress when user completes a video
     */
    public EnrollmentResponseDTO completeVideo(String userId, String courseId) {
        logger.info("Marking video as completed for user {} in course {}", userId, courseId);

        Enrollment enrollment = enrollmentRepo.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found for user and course"));

        enrollment.completeVideo();
        Enrollment updatedEnrollment = enrollmentRepo.save(enrollment);

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));

        logger.info("Video completed for user {} in course {}. Progress: {}%", 
                   userId, courseId, updatedEnrollment.getProgressPercentage());

        return mapToResponseDTO(updatedEnrollment, course);
    }

    /**
     * Update last accessed time when user accesses course
     */
    public EnrollmentResponseDTO accessCourse(String userId, String courseId) {
        logger.info("User {} accessing course {}", userId, courseId);

        Enrollment enrollment = enrollmentRepo.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found for user and course"));

        enrollment.accessCourse();
        Enrollment updatedEnrollment = enrollmentRepo.save(enrollment);

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));

        return mapToResponseDTO(updatedEnrollment, course);
    }

    /**
     * Get completed enrollments for a user
     */
    public List<EnrollmentResponseDTO> getCompletedEnrollments(String userId) {
        logger.info("Fetching completed enrollments for user {}", userId);

        List<Enrollment> enrollments = enrollmentRepo.findByUserIdAndIsCompletedOrderByCompletedAtDesc(userId, true);
        
        return enrollments.stream()
                .map(this::mapToResponseDTOWithCourse)
                .collect(Collectors.toList());
    }

    /**
     * Get recent enrollments for dashboard
     */
    public List<EnrollmentResponseDTO> getRecentEnrollments(String userId) {
        logger.info("Fetching recent enrollments for user {}", userId);

        List<Enrollment> enrollments = enrollmentRepo.findTop5ByUserIdOrderByLastAccessedAtDesc(userId);
        
        return enrollments.stream()
                .map(this::mapToResponseDTOWithCourse)
                .collect(Collectors.toList());
    }

    /**
     * Check if user is enrolled in a course
     */
    public boolean isUserEnrolled(String userId, String courseId) {
        return enrollmentRepo.existsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * Get enrollment statistics for a user
     */
    public EnrollmentStats getUserEnrollmentStats(String userId) {
        long totalEnrollments = enrollmentRepo.countByUserId(userId);
        long completedEnrollments = enrollmentRepo.countByUserIdAndIsCompleted(userId, true);
        
        return new EnrollmentStats(totalEnrollments, completedEnrollments);
    }

    /**
     * Update total videos count for all enrollments in a course
     */
    public void updateTotalVideosForCourse(String courseId) {
        logger.info("Updating total videos count for course {}", courseId);

        int totalVideos = (int) videoRepo.countByCourseId(courseId);
        List<Enrollment> enrollments = enrollmentRepo.findByCourseIdOrderByEnrolledAtDesc(courseId);
        
        for (Enrollment enrollment : enrollments) {
            enrollment.setTotalVideos(totalVideos);
        }
        
        enrollmentRepo.saveAll(enrollments);
        
        logger.info("Updated total videos count to {} for {} enrollments in course {}", 
                   totalVideos, enrollments.size(), courseId);
    }

    /**
     * Map Enrollment to EnrollmentResponseDTO with course details
     */
    private EnrollmentResponseDTO mapToResponseDTOWithCourse(Enrollment enrollment) {
        Course course = courseRepo.findById(enrollment.getCourseId()).orElse(null);
        return mapToResponseDTO(enrollment, course);
    }

    /**
     * Map Enrollment to EnrollmentResponseDTO
     */
    private EnrollmentResponseDTO mapToResponseDTO(Enrollment enrollment, Course course) {
        EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
        dto.setId(enrollment.getId());
        dto.setUserId(enrollment.getUserId());
        dto.setCourseId(enrollment.getCourseId());
        dto.setEnrolledAt(enrollment.getEnrolledAt());
        dto.setLastAccessedAt(enrollment.getLastAccessedAt());
        dto.setCompleted(enrollment.isCompleted());
        dto.setCompletedAt(enrollment.getCompletedAt());
        dto.setProgressPercentage(enrollment.getProgressPercentage());
        dto.setCompletedVideos(enrollment.getCompletedVideos());
        dto.setTotalVideos(enrollment.getTotalVideos());

        if (course != null) {
            dto.setCourseTitle(course.getTitle());
            dto.setCourseDescription(course.getDescription());
            dto.setCourseThumbnail(course.getThumbnailUrl());
        }

        return dto;
    }

    /**
     * Inner class for enrollment statistics
     */
    public static class EnrollmentStats {
        private final long totalEnrollments;
        private final long completedEnrollments;

        public EnrollmentStats(long totalEnrollments, long completedEnrollments) {
            this.totalEnrollments = totalEnrollments;
            this.completedEnrollments = completedEnrollments;
        }

        public long getTotalEnrollments() {
            return totalEnrollments;
        }

        public long getCompletedEnrollments() {
            return completedEnrollments;
        }

        public double getCompletionRate() {
            return totalEnrollments > 0 ? (double) completedEnrollments / totalEnrollments * 100 : 0;
        }
    }
}
