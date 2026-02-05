package com.student.studentcoursemanagement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "email_otps")
public class EmailOtp {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String otp;

    private LocalDateTime createdAt;

    @Indexed(expireAfterSeconds = 600) // Auto-delete after 10 minutes
    private LocalDateTime expiresAt;

    private boolean verified;

    private int attemptCount;
}
