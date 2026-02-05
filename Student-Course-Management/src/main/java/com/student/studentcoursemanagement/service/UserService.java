package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.UpdateUserProfileRequest;
import com.student.studentcoursemanagement.dto.UserProfileResponse;
import com.student.studentcoursemanagement.model.User;
import com.student.studentcoursemanagement.model.UserRole;
import com.student.studentcoursemanagement.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Get user profile by email
     */
    public ApiResponse<UserProfileResponse> getUserProfile(String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail);

            if (user == null) {
                return new ApiResponse<>(false, "User not found", null, 404);
            }

            UserProfileResponse profileResponse = buildProfileResponse(user);

            return new ApiResponse<>(true, "Profile retrieved successfully", profileResponse, 200);

        } catch (Exception e) {
            logger.error("Error getting user profile: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Error retrieving profile: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Get user profile by ID
     */
    public ApiResponse<UserProfileResponse> getUserProfileById(String userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return new ApiResponse<>(false, "User not found", null, 404);
            }

            UserProfileResponse profileResponse = buildProfileResponse(user);

            return new ApiResponse<>(true, "Profile retrieved successfully", profileResponse, 200);

        } catch (Exception e) {
            logger.error("Error getting user profile: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Error retrieving profile: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Update user profile by email
     */
    public ApiResponse<UserProfileResponse> updateUserProfile(String userEmail, UpdateUserProfileRequest request) {
        try {
            User user = userRepository.findByEmail(userEmail);

            if (user == null) {
                return new ApiResponse<>(false, "User not found", null, 404);
            }

            return updateUser(user, request);

        } catch (Exception e) {
            logger.error("Error updating user profile: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Error updating profile: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Update user profile by ID
     */
    public ApiResponse<UserProfileResponse> updateUserProfileById(String userId, UpdateUserProfileRequest request) {
        try {
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return new ApiResponse<>(false, "User not found", null, 404);
            }

            return updateUser(user, request);

        } catch (Exception e) {
            logger.error("Error updating user profile: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Error updating profile: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Common method to update user
     */
    private ApiResponse<UserProfileResponse> updateUser(User user, UpdateUserProfileRequest request) {
        // Update username if provided
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user.setUsername(request.getUsername().trim());
        }

        // Update mobile number if provided
        if (request.getMobileNumber() != null && !request.getMobileNumber().trim().isEmpty()) {
            user.setMobileNumber(request.getMobileNumber().trim());
        }

        // Update password if both current and new passwords are provided
        if (request.getCurrentPassword() != null && request.getNewPassword() != null) {
            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return new ApiResponse<>(false, "Current password is incorrect", null, 400);
            }
            // Update to new password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        UserProfileResponse profileResponse = buildProfileResponse(updatedUser);

        return new ApiResponse<>(true, "Profile updated successfully", profileResponse, 200);
    }

    /**
     * Helper method to build profile response from user entity
     */
    private UserProfileResponse buildProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .mobileNumber(user.getMobileNumber())
                .verified(user.isVerified())
                .authProvider(user.getAuthProvider() != null ? user.getAuthProvider().toString() : "LOCAL")
                .roles(user.getRoles().stream()
                        .map(UserRole::toString)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
