package com.student.studentcoursemanagement.controller;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.AuthResponse;
import com.student.studentcoursemanagement.dto.LoginRequestDTO;
import com.student.studentcoursemanagement.dto.RegisterRequestDTO;
import com.student.studentcoursemanagement.dto.GoogleAuthRequest;
import com.student.studentcoursemanagement.dto.ForgotPasswordRequest;
import com.student.studentcoursemanagement.dto.VerifyOtpRequest;
import com.student.studentcoursemanagement.dto.ResetPasswordRequest;
import com.student.studentcoursemanagement.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequestDTO request) {

        logger.info("Registration attempt for email: {}", request.getEmail());

        ApiResponse<AuthResponse> response = authService.register(request);

        if (!response.isSuccess()) {
            logger.error("Registration failed for email: {}. Reason: {}", request.getEmail(), response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 400;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("User registered successfully: {}", request.getEmail());
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 201;
        return ResponseEntity.status(statusCode).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequestDTO request) {

        logger.info("Login request for email: {}", request.getEmail());

        ApiResponse<AuthResponse> response = authService.login(request);

        if (!response.isSuccess()) {
            logger.error("Login failed for email: {}. Reason: {}", request.getEmail(), response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 401;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("User logged in successfully: {}", request.getEmail());
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        logger.info("Google login attempt");
        ApiResponse<AuthResponse> response = authService.googleLogin(request);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : (response.isSuccess() ? 200 : 401);
        return ResponseEntity.status(statusCode).body(response);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<ApiResponse<String>> verifyPassword(
            @Valid @RequestBody com.student.studentcoursemanagement.dto.VerifyPasswordRequest request) {
        logger.info("Password verification attempt for email: {}", request.getEmail());

        ApiResponse<String> response = authService.verifyPassword(request);

        if (!response.isSuccess()) {
            logger.error("Password verification failed for email: {}. Reason: {}", request.getEmail(),
                    response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 401;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("Password verified successfully for email: {}", request.getEmail());
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @PostMapping("/complete-login")
    public ResponseEntity<ApiResponse<AuthResponse>> completeLogin(
            @Valid @RequestBody com.student.studentcoursemanagement.dto.CompleteLoginRequest request) {
        logger.info("Complete login attempt for email: {}", request.getEmail());

        ApiResponse<AuthResponse> response = authService.completeLogin(request);

        if (!response.isSuccess()) {
            logger.error("Complete login failed for email: {}. Reason: {}", request.getEmail(), response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 401;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("User logged in successfully: {}", request.getEmail());
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @PostMapping("/login-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithOtp(
            @Valid @RequestBody com.student.studentcoursemanagement.dto.OtpLoginRequest request) {
        logger.info("OTP login attempt for email: {}", request.getEmail());

        ApiResponse<AuthResponse> response = authService.loginWithOtp(request);

        if (!response.isSuccess()) {
            logger.error("OTP login failed for email: {}. Reason: {}", request.getEmail(), response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 401;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("User logged in successfully via OTP: {}", request.getEmail());
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteAccount(@PathVariable String userId) {

        logger.info("Delete account request for user ID: {}", userId);

        ApiResponse<String> response = authService.deleteAccount(userId);

        if (!response.isSuccess()) {
            logger.error("Account deletion failed for user ID: {}. Reason: {}", userId, response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 400;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("Account deleted successfully for user ID: {}", userId);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @PutMapping("/admin/add-role/{userId}")
    public ResponseEntity<ApiResponse<String>> addAdminRole(@PathVariable String userId) {

        logger.info("Add admin role request for user ID: {}", userId);

        ApiResponse<String> response = authService.addAdminRole(userId);

        if (!response.isSuccess()) {
            logger.error("Adding admin role failed for user ID: {}. Reason: {}", userId, response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 400;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("Admin role added successfully for user ID: {}", userId);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Forgot password request for email: {}", request.getEmail());

        ApiResponse<String> response = authService.forgotPassword(request);

        if (!response.isSuccess()) {
            logger.error("Forgot password failed for email: {}. Reason: {}", request.getEmail(), response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 400;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("OTP sent successfully for password reset to: {}", request.getEmail());
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<String>> verifyResetOtp(@Valid @RequestBody VerifyOtpRequest request) {
        logger.info("Verify reset OTP request for email: {}", request.getEmail());

        ApiResponse<String> response = authService.verifyResetOtp(request);

        if (!response.isSuccess()) {
            logger.error("OTP verification failed for email: {}. Reason: {}", request.getEmail(), response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 400;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("OTP verified successfully for: {}", request.getEmail());
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        logger.info("Reset password request for email: {}", request.getEmail());

        ApiResponse<String> response = authService.resetPassword(request);

        if (!response.isSuccess()) {
            logger.error("Password reset failed for email: {}. Reason: {}", request.getEmail(), response.getMessage());
            int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 400;
            return ResponseEntity.status(statusCode).body(response);
        }

        logger.info("Password reset successfully for: {}", request.getEmail());
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;
        return ResponseEntity.status(statusCode).body(response);
    }
}
