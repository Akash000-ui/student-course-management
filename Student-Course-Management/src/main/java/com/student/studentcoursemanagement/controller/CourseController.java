package com.student.studentcoursemanagement.controller;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.CourseRequestDTO;
import com.student.studentcoursemanagement.dto.CourseResponseDTO;
import com.student.studentcoursemanagement.model.DifficultyLevel;
import com.student.studentcoursemanagement.service.CourseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponseDTO>> createCourse(
            @Valid @RequestBody CourseRequestDTO request) {

        logger.info("Create course request received for title: {}", request.getTitle());

        ApiResponse<CourseResponseDTO> response = courseService.createCourse(request);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 201;

        return ResponseEntity.status(statusCode).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getAllCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) String search) {

        logger.info("Get all courses request with filters - category: {}, difficulty: {}, search: {}",
                category, difficulty, search);

        ApiResponse<List<CourseResponseDTO>> response = courseService.getAllCourses(category, difficulty, search);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getCourseById(@PathVariable String id) {

        logger.info("Get course by ID request: {}", id);

        ApiResponse<CourseResponseDTO> response = courseService.getCourseById(id);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> updateCourse(
            @PathVariable String id,
            @Valid @RequestBody CourseRequestDTO request) {

        logger.info("Update course request for ID: {}", id);

        ApiResponse<CourseResponseDTO> response = courseService.updateCourse(id, request);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCourse(@PathVariable String id) {

        logger.info("Delete course request for ID: {}", id);

        ApiResponse<String> response = courseService.deleteCourse(id);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    // @GetMapping("/my-courses")
    // public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getMyCourses(
    // @RequestHeader(value = "X-User-Id", required = false, defaultValue =
    // "system") String userId) {
    //
    // logger.info("Get my courses request for user: {}", userId);
    //
    // ApiResponse<List<CourseResponseDTO>> response =
    // courseService.getCoursesByCreator(userId);
    // int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() :
    // 200;
    //
    // return ResponseEntity.status(statusCode).body(response);
    // }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Course service is running");
    }
}
