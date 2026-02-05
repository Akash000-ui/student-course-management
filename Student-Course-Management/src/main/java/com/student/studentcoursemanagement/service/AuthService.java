package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.AuthResponse;
import com.student.studentcoursemanagement.dto.GoogleAuthRequest;
import com.student.studentcoursemanagement.dto.LoginRequestDTO;
import com.student.studentcoursemanagement.dto.RegisterRequestDTO;
import com.student.studentcoursemanagement.dto.UserResponse;
import com.student.studentcoursemanagement.dto.ForgotPasswordRequest;
import com.student.studentcoursemanagement.dto.VerifyOtpRequest;
import com.student.studentcoursemanagement.dto.ResetPasswordRequest;
import com.student.studentcoursemanagement.model.AuthProvider;
import com.student.studentcoursemanagement.model.User;
import com.student.studentcoursemanagement.model.UserRole;
import com.student.studentcoursemanagement.repo.UserRepo;
import com.student.studentcoursemanagement.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private OtpService otpService;

    @Value("${google.clientId:}")
    private String googleClientId;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public ApiResponse<AuthResponse> register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Email already exists", null);
            response.setStatusCode(400);
            return response;
        }

        if (request.getEmail().isEmpty()) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Email is required", null);
            response.setStatusCode(400);
            return response;
        }
        if (request.getUsername().isEmpty()) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Username is required", null);
            response.setStatusCode(400);
            return response;
        }

        if (request.getMobileNumber() == null || request.getMobileNumber().isEmpty()) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Mobile number is required", null);
            response.setStatusCode(400);
            return response;
        }

        // Check if email is verified via OTP
        if (!otpService.isEmailVerified(request.getEmail())) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false,
                    "Email not verified. Please verify your email with OTP first.", null);
            response.setStatusCode(403);
            return response;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobileNumber(request.getMobileNumber());
        user.setVerified(true);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        user.getRoles().add(UserRole.USER);

        userRepository.save(user);

        // Clean up OTP after successful registration
        otpService.deleteOtp(request.getEmail());

        AuthResponse authResponse = AuthResponse.builder()
                .token(jwtUtil.generateToken(user.getId(), user.getEmail()))
                .user(new UserResponse(user))
                .build();
        if (authResponse.getToken() == null) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Token generation failed", null);
            response.setStatusCode(500);
            return response;
        }

        ApiResponse<AuthResponse> response = new ApiResponse<>(true, "User registered successfully", authResponse);
        response.setStatusCode(201);
        return response;
    }

    public ApiResponse<AuthResponse> login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "User not found", null);
            response.setStatusCode(404);
            return response;
        }

        logger("Login attempt for user: {}", user);

        // If the account is a Google account, require Google sign-in
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false,
                    "This account uses Google sign-in. Please sign in with Google.", null);
            response.setStatusCode(409);
            return response;
        }

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Invalid credentials", null);
            response.setStatusCode(401);
            return response;
        }

        // Generate token
        AuthResponse authResponse = AuthResponse.builder()
                .token(jwtUtil.generateToken(user.getId(), user.getEmail()))
                .user(new UserResponse(user))
                .build();

        if (authResponse.getToken() == null) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Token generation failed", null);
            response.setStatusCode(500);
            return response;
        }

        ApiResponse<AuthResponse> response = new ApiResponse<>(true, "Login successful", authResponse);
        response.setStatusCode(200);
        return response;
    }

    /**
     * Verify password without logging in - for 2FA flow
     */
    public ApiResponse<String> verifyPassword(com.student.studentcoursemanagement.dto.VerifyPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            ApiResponse<String> response = new ApiResponse<>(false, "User not found", null);
            response.setStatusCode(404);
            return response;
        }

        // If the account is a Google account, require Google sign-in
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            ApiResponse<String> response = new ApiResponse<>(false,
                    "This account uses Google sign-in. Please sign in with Google.", null);
            response.setStatusCode(409);
            return response;
        }

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            ApiResponse<String> response = new ApiResponse<>(false, "Invalid credentials", null);
            response.setStatusCode(401);
            return response;
        }

        ApiResponse<String> response = new ApiResponse<>(true, "Password verified successfully", null);
        response.setStatusCode(200);
        return response;
    }

    /**
     * Complete login with OTP verification - for 2FA flow
     */
    public ApiResponse<AuthResponse> completeLogin(
            com.student.studentcoursemanagement.dto.CompleteLoginRequest request) {
        // First verify OTP
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Invalid or expired OTP", null);
            response.setStatusCode(401);
            return response;
        }

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "User not found", null);
            response.setStatusCode(404);
            return response;
        }

        // Clean up OTP after successful verification
        otpService.deleteOtp(request.getEmail());

        // Generate token
        AuthResponse authResponse = AuthResponse.builder()
                .token(jwtUtil.generateToken(user.getId(), user.getEmail()))
                .user(new UserResponse(user))
                .build();

        if (authResponse.getToken() == null) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Token generation failed", null);
            response.setStatusCode(500);
            return response;
        }

        ApiResponse<AuthResponse> response = new ApiResponse<>(true, "Login successful", authResponse);
        response.setStatusCode(200);
        return response;
    }

    public ApiResponse<AuthResponse> loginWithOtp(com.student.studentcoursemanagement.dto.OtpLoginRequest request) {
        // First verify OTP
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Invalid or expired OTP", null);
            response.setStatusCode(401);
            return response;
        }

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "User not found", null);
            response.setStatusCode(404);
            return response;
        }

        // Clean up OTP after successful verification
        otpService.deleteOtp(request.getEmail());

        // Generate token
        AuthResponse authResponse = AuthResponse.builder()
                .token(jwtUtil.generateToken(user.getId(), user.getEmail()))
                .user(new UserResponse(user))
                .build();

        if (authResponse.getToken() == null) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Token generation failed", null);
            response.setStatusCode(500);
            return response;
        }

        ApiResponse<AuthResponse> response = new ApiResponse<>(true, "Login successful via OTP", authResponse);
        response.setStatusCode(200);
        return response;
    }

    private void logger(String s, User user) {
        logger.info(s, user.getRoles());
    }

    public ApiResponse<String> deleteAccount(String userId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            ApiResponse<String> response = new ApiResponse<>(false, "User not found", null);
            response.setStatusCode(404);
            return response;
        }

        try {
            userRepository.delete(user);
            ApiResponse<String> response = new ApiResponse<>(true, "Account deleted successfully", null);
            response.setStatusCode(200);
            return response;
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(false, "Failed to delete account", null);
            response.setStatusCode(500);
            return response;
        }
    }

    public ApiResponse<String> addAdminRole(String userId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            ApiResponse<String> response = new ApiResponse<>(false, "User not found", null);
            response.setStatusCode(404);
            return response;
        }

        if (user.getRoles().contains(UserRole.ADMIN)) {
            ApiResponse<String> response = new ApiResponse<>(false, "User is already an admin", null);
            response.setStatusCode(400);
            return response;
        }

        user.getRoles().add(UserRole.ADMIN);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        ApiResponse<String> response = new ApiResponse<>(true, "Admin role added successfully", null);
        response.setStatusCode(200);
        return response;
    }

    public ApiResponse<AuthResponse> googleLogin(GoogleAuthRequest request) {
        // Verify the ID token with Google
        try {
            String expectedClientId = Optional.ofNullable(request.getClientId()).filter(s -> !s.isBlank())
                    .orElse(googleClientId);
            if (expectedClientId == null || expectedClientId.isBlank()) {
                ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Google clientId not configured", null);
                response.setStatusCode(500);
                return response;
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(expectedClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Invalid Google ID token", null);
                response.setStatusCode(401);
                return response;
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = Optional.ofNullable((String) payload.get("name")).orElse(email);

            User user = userRepository.findByEmail(email);
            if (user == null) {
                // Create new user as GOOGLE provider
                user = User.builder()
                        .email(email)
                        .username(name)
                        .verified(true)
                        .authProvider(AuthProvider.GOOGLE)
                        .createdAt(java.time.LocalDateTime.now())
                        .updatedAt(java.time.LocalDateTime.now())
                        .build();
                user.getRoles().add(UserRole.USER);
            } else {
                // If existing LOCAL account, block sign-in via Google to avoid account takeover
                if (user.getAuthProvider() == AuthProvider.LOCAL) {
                    ApiResponse<AuthResponse> response = new ApiResponse<>(false,
                            "Account registered with email/password. Please sign in with password.", null);
                    response.setStatusCode(409);
                    return response;
                }
                // Ensure provider is GOOGLE and mark verified
                user.setAuthProvider(AuthProvider.GOOGLE);
                user.setVerified(true);
                user.setUpdatedAt(java.time.LocalDateTime.now());
                if (user.getUsername() == null || user.getUsername().isBlank()) {
                    user.setUsername(name);
                }
                if (!user.getRoles().contains(UserRole.USER)) {
                    user.getRoles().add(UserRole.USER);
                }
            }

            userRepository.save(user);

            AuthResponse authResponse = AuthResponse.builder()
                    .token(jwtUtil.generateToken(user.getId(), user.getEmail()))
                    .user(new UserResponse(user))
                    .build();

            ApiResponse<AuthResponse> response = new ApiResponse<>(true, "Login successful", authResponse);
            response.setStatusCode(200);
            return response;
        } catch (java.security.GeneralSecurityException | java.io.IOException e) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Google login failed", null);
            response.setStatusCode(500);
            return response;
        }
    }

    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            ApiResponse<String> response = new ApiResponse<>(false, "User not found with this email", null);
            response.setStatusCode(404);
            return response;
        }

        // Check if user is using LOCAL auth provider
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            ApiResponse<String> response = new ApiResponse<>(false, 
                "Password reset is only available for email/password accounts. You signed up with " + 
                user.getAuthProvider().toString(), null);
            response.setStatusCode(400);
            return response;
        }

        // Generate and send OTP
        otpService.generateAndSendOtp(request.getEmail());

        ApiResponse<String> response = new ApiResponse<>(true, 
            "OTP sent successfully to your email for password reset", null);
        response.setStatusCode(200);
        return response;
    }

    public ApiResponse<String> verifyResetOtp(VerifyOtpRequest request) {
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            ApiResponse<String> response = new ApiResponse<>(false, "Invalid or expired OTP", null);
            response.setStatusCode(400);
            return response;
        }

        // Mark OTP as verified for password reset
        ApiResponse<String> response = new ApiResponse<>(true, "OTP verified successfully. You can now reset your password.", null);
        response.setStatusCode(200);
        return response;
    }

    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        // Verify OTP again for security
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            ApiResponse<String> response = new ApiResponse<>(false, "Invalid or expired OTP", null);
            response.setStatusCode(400);
            return response;
        }

        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            ApiResponse<String> response = new ApiResponse<>(false, "User not found", null);
            response.setStatusCode(404);
            return response;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        // Delete OTP after successful password reset
        otpService.deleteOtp(request.getEmail());

        ApiResponse<String> response = new ApiResponse<>(true, "Password reset successfully", null);
        response.setStatusCode(200);
        return response;
    }
}
