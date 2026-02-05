package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.EmailOtp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailOtpRepo extends MongoRepository<EmailOtp, String> {
    Optional<EmailOtp> findByEmail(String email);

    void deleteByEmail(String email);
}
