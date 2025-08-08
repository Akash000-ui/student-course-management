package com.student.studentcoursemanagement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String username;
    private String password;
    @Builder.Default
    private boolean verified = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();


    public User(String email, String username, String password) {
        this();
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public User(String email, String username, AuthProvider authProvider) {
        this();
        this.email = email;
        this.username = username;
        this.authProvider = authProvider;
        this.verified = true;
    }
}