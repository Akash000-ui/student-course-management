package com.student.studentcoursemanagement.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private UserResponse user;

    public AuthResponse() {}

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }
}
