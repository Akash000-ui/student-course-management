package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends MongoRepository<User , String> {

    boolean existsByEmail(String email);
    User findByEmail(String email);
}
