package com.student.studentcoursemanagement.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.student.studentcoursemanagement.model.UserVideoCompletion;

@Repository
public interface UserVideoCompletionRepo extends MongoRepository<UserVideoCompletion, String> {

    boolean existsByUserIdAndVideoId(String userId, String videoId);

    long countByUserIdAndCourseId(String userId, String courseId);

    List<UserVideoCompletion> findByUserIdAndCourseId(String userId, String courseId);

    /**
     * Delete all completion records for a course
     */
    void deleteByCourseId(String courseId);
}
