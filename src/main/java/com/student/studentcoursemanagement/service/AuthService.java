package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.AuthResponse;
import com.student.studentcoursemanagement.dto.LoginRequestDTO;
import com.student.studentcoursemanagement.dto.RegisterRequestDTO;
import com.student.studentcoursemanagement.dto.UserResponse;
import com.student.studentcoursemanagement.model.User;
import com.student.studentcoursemanagement.model.UserRole;
import com.student.studentcoursemanagement.repo.UserRepo;
import com.student.studentcoursemanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepository;

    public ApiResponse<AuthResponse> register(RegisterRequestDTO request){

        if (userRepository.existsByEmail(request.getEmail())) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Email already exists", null);
            response.setStatusCode(400);
            return response;
        }

        if (request.getEmail().isEmpty()){
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Email is required", null);
            response.setStatusCode(400);
            return response;
        }
        if (request.getUsername().isEmpty()) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(false, "Username is required", null);
            response.setStatusCode(400);
            return response;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        user.getRoles().add(UserRole.USER);
        
        userRepository.save(user);
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

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
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
}
