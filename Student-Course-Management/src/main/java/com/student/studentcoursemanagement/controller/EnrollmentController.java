package com.student.studentcoursemanagement.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.EnrollmentRequestDTO;
import com.student.studentcoursemanagement.dto.EnrollmentResponseDTO;
import com.student.studentcoursemanagement.service.EnrollmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

        private static final Logger logger = LoggerFactory.getLogger(EnrollmentController.class);

        @Autowired
        private EnrollmentService enrollmentService;

        @PostMapping
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> enrollInCourse(
                        @Valid @RequestBody EnrollmentRequestDTO request,
                        Authentication authentication) {

                logger.info("Enrollment request for course: {}", request.getCourseId());

                String userId = authentication.getName();
                EnrollmentResponseDTO enrollment = enrollmentService.enrollInCourse(userId, request);

                ApiResponse<EnrollmentResponseDTO> response = new ApiResponse<>(
                                true,
                                "Successfully enrolled in course",
                                enrollment);
                response.setStatusCode(201);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Get all enrollments for current user
         * GET /api/enrollments
         */
        @GetMapping
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getUserEnrollments(
                        Authentication authentication) {

                String userId = authentication.getName();
                logger.info("Fetching enrollments for user: {}", userId);

                List<EnrollmentResponseDTO> enrollments = enrollmentService.getUserEnrollments(userId);

                ApiResponse<List<EnrollmentResponseDTO>> response = new ApiResponse<>(
                                true,
                                "Enrollments retrieved successfully",
                                enrollments);
                response.setStatusCode(200);

                return ResponseEntity.ok(response);
        }

        /**
         * Get specific enrollment by course ID
         * GET /api/enrollments/course/{courseId}
         */
        @GetMapping("/course/{courseId}")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> getEnrollmentByCourse(
                        @PathVariable String courseId,
                        Authentication authentication) {

                String userId = authentication.getName();
                logger.info("Fetching enrollment for user: {} and course: {}", userId, courseId);

                EnrollmentResponseDTO enrollment = enrollmentService.getEnrollment(userId, courseId);

                ApiResponse<EnrollmentResponseDTO> response = new ApiResponse<>(
                                true,
                                "Enrollment retrieved successfully",
                                enrollment);
                response.setStatusCode(200);

                return ResponseEntity.ok(response);
        }

        /**
         * Access a course (update last accessed time)
         * PUT /api/enrollments/course/{courseId}/access
         */
        @PutMapping("/course/{courseId}/access")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> accessCourse(
                        @PathVariable String courseId,
                        Authentication authentication) {

                String userId = authentication.getName();
                logger.info("User: {} accessing course: {}", userId, courseId);

                EnrollmentResponseDTO enrollment = enrollmentService.accessCourse(userId, courseId);

                ApiResponse<EnrollmentResponseDTO> response = new ApiResponse<>(
                                true,
                                "Course accessed successfully",
                                enrollment);
                response.setStatusCode(200);

                return ResponseEntity.ok(response);
        }

        /**
         * Get completed enrollments for current user
         * GET /api/enrollments/completed
         */
        @GetMapping("/completed")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getCompletedEnrollments(
                        Authentication authentication) {

                String userId = authentication.getName();
                logger.info("Fetching completed enrollments for user: {}", userId);

                List<EnrollmentResponseDTO> enrollments = enrollmentService.getCompletedEnrollments(userId);

                ApiResponse<List<EnrollmentResponseDTO>> response = new ApiResponse<>(
                                true,
                                "Completed enrollments retrieved successfully",
                                enrollments);
                response.setStatusCode(200);

                return ResponseEntity.ok(response);
        }

        /**
         * Get recent enrollments for dashboard
         * GET /api/enrollments/recent
         */
        @GetMapping("/recent")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getRecentEnrollments(
                        Authentication authentication) {

                String userId = authentication.getName();
                logger.info("Fetching recent enrollments for user: {}", userId);

                List<EnrollmentResponseDTO> enrollments = enrollmentService.getRecentEnrollments(userId);

                ApiResponse<List<EnrollmentResponseDTO>> response = new ApiResponse<>(
                                true,
                                "Recent enrollments retrieved successfully",
                                enrollments);
                response.setStatusCode(200);

                return ResponseEntity.ok(response);
        }

        /**
         * Check if user is enrolled in a course
         * GET /api/enrollments/check/{courseId}
         */
        @GetMapping("/check/{courseId}")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Boolean>> checkEnrollment(
                        @PathVariable String courseId,
                        Authentication authentication) {

                String userId = authentication.getName();
                logger.info("Checking enrollment for user: {} in course: {}", userId, courseId);

                boolean isEnrolled = enrollmentService.isUserEnrolled(userId, courseId);

                ApiResponse<Boolean> response = new ApiResponse<>(
                                true,
                                "Enrollment status checked",
                                isEnrolled);
                response.setStatusCode(200);

                return ResponseEntity.ok(response);
        }

        /**
         * Get enrollment statistics for current user
         * GET /api/enrollments/stats
         */
        @GetMapping("/stats")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<EnrollmentService.EnrollmentStats>> getEnrollmentStats(
                        Authentication authentication) {

                String userId = authentication.getName();
                logger.info("Fetching enrollment statistics for user: {}", userId);

                EnrollmentService.EnrollmentStats stats = enrollmentService.getUserEnrollmentStats(userId);

                ApiResponse<EnrollmentService.EnrollmentStats> response = new ApiResponse<>(
                                true,
                                "Enrollment statistics retrieved successfully",
                                stats);
                response.setStatusCode(200);

                return ResponseEntity.ok(response);
        }
}
