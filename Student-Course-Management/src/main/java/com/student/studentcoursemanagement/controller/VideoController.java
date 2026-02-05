package com.student.studentcoursemanagement.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.VideoRequestDTO;
import com.student.studentcoursemanagement.dto.VideoResponseDTO;
import com.student.studentcoursemanagement.service.VideoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoService videoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VideoResponseDTO>> createVideo(@Valid @RequestBody VideoRequestDTO request) {

        logger.info("Create video request received for title: {} in course: {}", request.getTitle(),
                request.getCourseId());

        ApiResponse<VideoResponseDTO> response = videoService.createVideo(request);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 201;

        return ResponseEntity.status(statusCode).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VideoResponseDTO>> updateVideo(
            @PathVariable String id,
            @Valid @RequestBody VideoRequestDTO request) {

        logger.info("Update video request received for ID: {}", id);

        ApiResponse<VideoResponseDTO> response = videoService.updateVideo(id, request);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    @PatchMapping("/{id}/position")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VideoResponseDTO>> updateVideoPosition(
            @PathVariable String id,
            @RequestParam("position") int position) {
        logger.info("Update video position request for ID: {} to position {}", id, position);
        ApiResponse<VideoResponseDTO> response = videoService.updateVideoPosition(id, position);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VideoResponseDTO>> getVideoById(@PathVariable String id) {

        logger.info("Get video request received for ID: {}", id);

        ApiResponse<VideoResponseDTO> response = videoService.getVideoById(id);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VideoResponseDTO>>> getVideosByCourseId(@PathVariable String courseId) {

        logger.info("Get videos by course ID request received for course: {}", courseId);

        ApiResponse<List<VideoResponseDTO>> response = videoService.getVideosByCourseId(courseId);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable String id) {

        logger.info("Delete video request received for ID: {}", id);

        ApiResponse<Void> response = videoService.deleteVideo(id);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }
}
