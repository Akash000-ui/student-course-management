package com.student.studentcoursemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.student.studentcoursemanagement.model.User;
import com.student.studentcoursemanagement.model.UserRole;
import com.student.studentcoursemanagement.repo.UserRepo;
import com.student.studentcoursemanagement.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AdminValidationService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepo userRepo;


    public boolean isAdminFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        
        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            
            if (email == null || !jwtUtil.validateToken(token, email)) {
                return false;
            }
            
            User user = userRepo.findByEmail(email);
            return user != null && user.getRoles() != null && user.getRoles().contains(UserRole.ADMIN);
            
        } catch (Exception e) {
            return false;
        }
    }
}