package com.student.studentcoursemanagement.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.student.studentcoursemanagement.model.Video;

@Repository
public interface VideoRepo extends MongoRepository<Video, String> {
    
    // Find videos by course ID
    List<Video> findByCourseId(String courseId);
    
    // Find videos by course ID ordered by creation date
    List<Video> findByCourseIdOrderByCreatedAtAsc(String courseId);
    // Find videos by course ordered by position, fallback to createdAt if null
    List<Video> findByCourseIdOrderByPositionAsc(String courseId);
    
    // Count videos in a course
    long countByCourseId(String courseId);

    /**
     * Delete all videos for a course
     */
    void deleteByCourseId(String courseId);
}
