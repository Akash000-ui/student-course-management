package com.student.studentcoursemanagement.controller;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.UpdateUserProfileRequest;
import com.student.studentcoursemanagement.dto.UserProfileResponse;
import com.student.studentcoursemanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(Authentication authentication) {
        logger.info("Get profile request for user: {}", authentication.getName());

        String userId = authentication.getName(); // This is the user ID from JWT filter
        ApiResponse<UserProfileResponse> response = userService.getUserProfileById(userId);

        if (!response.isSuccess()) {
            logger.error("Failed to get profile for user: {}. Reason: {}", userId, response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 404;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("Profile retrieved successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request) {

        logger.info("Update profile request for user: {}", authentication.getName());

        String userId = authentication.getName(); // This is the user ID from JWT filter
        ApiResponse<UserProfileResponse> response = userService.updateUserProfileById(userId, request);

        if (!response.isSuccess()) {
            logger.error("Failed to update profile for user: {}. Reason: {}", userId, response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 400;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("Profile updated successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }
}
