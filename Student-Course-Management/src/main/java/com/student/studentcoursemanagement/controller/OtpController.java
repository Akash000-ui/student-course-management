package com.student.studentcoursemanagement.controller;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.SendOtpRequest;
import com.student.studentcoursemanagement.dto.VerifyOtpRequest;
import com.student.studentcoursemanagement.service.OtpService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    @Autowired
    private OtpService otpService;

    /**
     * Send OTP to email
     * POST /api/otp/send
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        try {
            logger.info("Sending OTP to email: {}", request.getEmail());
            otpService.generateAndSendOtp(request.getEmail());

            ApiResponse<String> response = new ApiResponse<>(
                    true,
                    "OTP sent successfully to " + request.getEmail(),
                    null,
                    200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending OTP to email: {}", request.getEmail(), e);
            ApiResponse<String> response = new ApiResponse<>(
                    false,
                    "Failed to send OTP: " + e.getMessage(),
                    null,
                    500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Verify OTP
     * POST /api/otp/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            logger.info("Verifying OTP for email: {}", request.getEmail());
            boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());

            if (isValid) {
                ApiResponse<Boolean> response = new ApiResponse<>(
                        true,
                        "OTP verified successfully",
                        true,
                        200);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Boolean> response = new ApiResponse<>(
                        false,
                        "Invalid or expired OTP",
                        false,
                        400);
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            logger.error("Error verifying OTP for email: {}", request.getEmail(), e);
            ApiResponse<Boolean> response = new ApiResponse<>(
                    false,
                    "Error verifying OTP: " + e.getMessage(),
                    false,
                    500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Resend OTP
     * POST /api/otp/resend
     */
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<String>> resendOtp(@Valid @RequestBody SendOtpRequest request) {
        try {
            logger.info("Resending OTP to email: {}", request.getEmail());
            otpService.generateAndSendOtp(request.getEmail());

            ApiResponse<String> response = new ApiResponse<>(
                    true,
                    "OTP resent successfully to " + request.getEmail(),
                    null,
                    200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error resending OTP to email: {}", request.getEmail(), e);
            ApiResponse<String> response = new ApiResponse<>(
                    false,
                    "Failed to resend OTP: " + e.getMessage(),
                    null,
                    500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Send OTP for login
     * POST /api/otp/send-login
     */
    @PostMapping("/send-login")
    public ResponseEntity<ApiResponse<String>> sendLoginOtp(@Valid @RequestBody SendOtpRequest request) {
        try {
            logger.info("Sending login OTP to email: {}", request.getEmail());
            otpService.generateAndSendOtp(request.getEmail());

            ApiResponse<String> response = new ApiResponse<>(
                    true,
                    "Login OTP sent successfully to " + request.getEmail(),
                    null,
                    200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending login OTP to email: {}", request.getEmail(), e);
            ApiResponse<String> response = new ApiResponse<>(
                    false,
                    "Failed to send login OTP: " + e.getMessage(),
                    null,
                    500);
            return ResponseEntity.status(500).body(response);
        }
    }
}
