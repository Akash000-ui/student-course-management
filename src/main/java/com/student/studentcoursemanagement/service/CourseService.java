package com.student.studentcoursemanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.CourseRequestDTO;
import com.student.studentcoursemanagement.dto.CourseResponseDTO;
import com.student.studentcoursemanagement.exception.CourseNotFoundException;
import com.student.studentcoursemanagement.exception.InvalidCourseDataException;
import com.student.studentcoursemanagement.model.Course;
import com.student.studentcoursemanagement.model.CourseCategory;
import com.student.studentcoursemanagement.model.DifficultyLevel;
import com.student.studentcoursemanagement.repo.CourseRepo;
import com.student.studentcoursemanagement.repo.EnrollmentRepo;
import com.student.studentcoursemanagement.repo.UserVideoCompletionRepo;
import com.student.studentcoursemanagement.repo.VideoRepo;

@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private CourseRepo courseRepository;

    @Autowired
    private VideoRepo videoRepository;

    @Autowired
    private EnrollmentRepo enrollmentRepository;

    @Autowired
    private UserVideoCompletionRepo completionRepository;

//    @Autowired
//    private FileUploadService fileUploadService;

    public ApiResponse<CourseResponseDTO> createCourse(CourseRequestDTO request, String createdBy) {
        try {
            logger.info("Creating new course with title: {}", request.getTitle());

            Course course = Course.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .category(request.getCategory())
                    .difficulty(request.getDifficulty())
                    .thumbnailUrl(request.getThumbnailUrl())
                    .createdBy(createdBy)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Course savedCourse = courseRepository.save(course);

            ApiResponse<CourseResponseDTO> response = new ApiResponse<>(
                    true,
                    "Course created successfully",
                    CourseResponseDTO.fromEntity(savedCourse));
            response.setStatusCode(201);

            logger.info("Course created successfully with ID: {}", savedCourse.getId());
            return response;

        } catch (Exception e) {
            logger.error("Error creating course: {}", e.getMessage(), e);
            throw new InvalidCourseDataException("Failed to create course: " + e.getMessage());
        }
    }

    public ApiResponse<List<CourseResponseDTO>> getAllCourses(
            CourseCategory category,
            DifficultyLevel difficulty,
            String search) {

        try {
            logger.info("Fetching courses with filters - category: {}, difficulty: {}, search: {}",
                    category, difficulty, search);

            List<Course> courses;

            // Apply filters based on provided parameters
            if (search != null && !search.trim().isEmpty()) {
                if (category != null && difficulty != null) {
                    courses = courseRepository.findByTitleContainingIgnoreCaseAndCategoryAndDifficulty(
                            search.trim(), category, difficulty);
                } else if (category != null) {
                    courses = courseRepository.findByTitleContainingIgnoreCaseAndCategory(
                            search.trim(), category);
                } else if (difficulty != null) {
                    courses = courseRepository.findByTitleContainingIgnoreCaseAndDifficulty(
                            search.trim(), difficulty);
                } else {
                    courses = courseRepository.findByTitleContainingIgnoreCase(search.trim());
                }
            } else if (category != null && difficulty != null) {
                courses = courseRepository.findByCategoryAndDifficulty(category, difficulty);
            } else if (category != null) {
                courses = courseRepository.findByCategory(category);
            } else if (difficulty != null) {
                courses = courseRepository.findByDifficulty(difficulty);
            } else {
                courses = courseRepository.findAll();
            }

            List<CourseResponseDTO> courseResponses = courses.stream()
                    .map(CourseResponseDTO::fromEntity)
                    .collect(Collectors.toList());

            ApiResponse<List<CourseResponseDTO>> response = new ApiResponse<>(
                    true,
                    "Courses retrieved successfully",
                    courseResponses);
            response.setStatusCode(200);

            logger.info("Retrieved {} courses", courseResponses.size());
            return response;

        } catch (Exception e) {
            logger.error("Error fetching courses: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch courses: " + e.getMessage());
        }
    }

    public ApiResponse<CourseResponseDTO> getCourseById(String id) {
        try {
            logger.info("Fetching course with ID: {}", id);

            Optional<Course> courseOpt = courseRepository.findById(id);
            if (courseOpt.isEmpty()) {
                throw new CourseNotFoundException("Course not found with ID: " + id);
            }

            Course course = courseOpt.get();
            ApiResponse<CourseResponseDTO> response = new ApiResponse<>(
                    true,
                    "Course retrieved successfully",
                    CourseResponseDTO.fromEntity(course));
            response.setStatusCode(200);

            logger.info("Course retrieved successfully with ID: {}", id);
            return response;

        } catch (CourseNotFoundException e) {
            logger.error("Course not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching course with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch course: " + e.getMessage());
        }
    }

    public ApiResponse<CourseResponseDTO> updateCourse(String id, CourseRequestDTO request) {
        try {
            logger.info("Updating course with ID: {}", id);

            Optional<Course> courseOpt = courseRepository.findById(id);
            if (courseOpt.isEmpty()) {
                throw new CourseNotFoundException("Course not found with ID: " + id);
            }

            Course existingCourse = courseOpt.get();

            // Update fields
            existingCourse.setTitle(request.getTitle());
            existingCourse.setDescription(request.getDescription());
            existingCourse.setCategory(request.getCategory());
            existingCourse.setDifficulty(request.getDifficulty());
            existingCourse.setThumbnailUrl(request.getThumbnailUrl());
            existingCourse.setUpdatedAt(LocalDateTime.now());

            Course updatedCourse = courseRepository.save(existingCourse);

            ApiResponse<CourseResponseDTO> response = new ApiResponse<>(
                    true,
                    "Course updated successfully",
                    CourseResponseDTO.fromEntity(updatedCourse));
            response.setStatusCode(200);

            logger.info("Course updated successfully with ID: {}", id);
            return response;

        } catch (CourseNotFoundException e) {
            logger.error("Course not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error updating course with ID {}: {}", id, e.getMessage(), e);
            throw new InvalidCourseDataException("Failed to update course: " + e.getMessage());
        }
    }

    public ApiResponse<String> deleteCourse(String id) {
        try {
            logger.info("Deleting course with ID: {}", id);

            Optional<Course> courseOpt = courseRepository.findById(id);
            if (courseOpt.isEmpty()) {
                throw new CourseNotFoundException("Course not found with ID: " + id);
            }

            Course course = courseOpt.get();

            videoRepository.deleteByCourseId(id);

            enrollmentRepository.deleteByCourseId(id);

            completionRepository.deleteByCourseId(id);

            // 4) If thumbnailUrl points to a locally stored file, try deleting it
//            String thumb = course.getThumbnailUrl();
//            if (thumb != null && !thumb.isBlank()) {
//                String lc = thumb.toLowerCase();
//                String toDelete = null;
//                final String dlPrefix = "/api/files/download/";
//                final String viewPrefix = "/api/files/view/";
//                if (lc.contains(dlPrefix)) {
//                    int idx = lc.indexOf(dlPrefix);
//                    toDelete = thumb.substring(idx + dlPrefix.length());
//                } else if (lc.contains(viewPrefix)) {
//                    int idx = lc.indexOf(viewPrefix);
//                    toDelete = thumb.substring(idx + viewPrefix.length());
//                } else if (!(lc.startsWith("http://") || lc.startsWith("https://"))) {
//                    // Probably a direct relative path saved in DB
//                    toDelete = thumb;
//                }
//
//                if (toDelete != null && !toDelete.isBlank()) {
//                    boolean deleted = fileUploadService.deleteFile(toDelete);
//                    if (deleted) {
//                        logger.info("Deleted local thumbnail {} for course {}", toDelete, id);
//                    } else {
//                        logger.warn("Could not delete thumbnail {} for course {} (may be external or already removed)",
//                                toDelete, id);
//                    }
//                }
//            }

            courseRepository.deleteById(id);

            ApiResponse<String> response = new ApiResponse<>(
                    true,
                    "Course deleted successfully",
                    null);
            response.setStatusCode(200);

            logger.info("Course and all dependent data deleted successfully for ID: {}", id);
            return response;

        } catch (CourseNotFoundException e) {
            logger.error("Course not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting course with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete course: " + e.getMessage());
        }
    }

    public ApiResponse<List<CourseResponseDTO>> getCoursesByCreator(String createdBy) {
        try {
            logger.info("Fetching courses created by user: {}", createdBy);

            List<Course> courses = courseRepository.findByCreatedBy(createdBy);
            List<CourseResponseDTO> courseResponses = courses.stream()
                    .map(CourseResponseDTO::fromEntity)
                    .collect(Collectors.toList());

            ApiResponse<List<CourseResponseDTO>> response = new ApiResponse<>(
                    true,
                    "Courses retrieved successfully",
                    courseResponses);
            response.setStatusCode(200);

            logger.info("Retrieved {} courses for user: {}", courseResponses.size(), createdBy);
            return response;

        } catch (Exception e) {
            logger.error("Error fetching courses for user {}: {}", createdBy, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch courses: " + e.getMessage());
        }
    }
}
