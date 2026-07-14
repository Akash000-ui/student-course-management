package com.student.studentcoursemanagement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.VideoCompletionResponse;
import com.student.studentcoursemanagement.service.UserVideoCompletionService;

@RestController
@RequestMapping("/api/progress")
public class UserVideoCompletionController {

    private static final Logger logger = LoggerFactory.getLogger(UserVideoCompletionController.class);

    @Autowired
    private UserVideoCompletionService completionService;

    @PutMapping("/courses/{courseId}/videos/{videoId}/complete")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VideoCompletionResponse>> markComplete(
            @PathVariable String courseId,
            @PathVariable String videoId,
            Authentication authentication) {
        String userId = authentication.getName();
        logger.info("User {} marking video {} complete in course {}", userId, videoId, courseId);
        var res = completionService.markCompleted(userId, courseId, videoId);
        return ResponseEntity.ok(new ApiResponse<>(true, res.isAlreadyCompleted() ? "Already completed" : "Marked completed", res, 200));
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VideoCompletionResponse>> progress(
            @PathVariable String courseId,
            Authentication authentication) {
        String userId = authentication.getName();
        var res = completionService.getProgress(userId, courseId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Progress fetched", res, 200));
    }
}
