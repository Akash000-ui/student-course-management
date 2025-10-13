package com.student.studentcoursemanagement.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.student.studentcoursemanagement.model.Enrollment;

@Repository
public interface EnrollmentRepo extends MongoRepository<Enrollment, String> {
    
    /**
     * Find enrollment by user ID and course ID
     */
    Optional<Enrollment> findByUserIdAndCourseId(String userId, String courseId);
    
    /**
     * Find all enrollments for a specific user
     */
    List<Enrollment> findByUserIdOrderByEnrolledAtDesc(String userId);
    
    /**
     * Find all completed enrollments for a user
     */
    List<Enrollment> findByUserIdAndIsCompletedOrderByCompletedAtDesc(String userId, boolean isCompleted);
    
    /**
     * Find all enrollments for a specific course
     */
    List<Enrollment> findByCourseIdOrderByEnrolledAtDesc(String courseId);
    
    /**
     * Count total enrollments for a course
     */
    long countByCourseId(String courseId);
    
    /**
     * Count total enrollments for a user
     */
    long countByUserId(String userId);
    
    /**
     * Count completed enrollments for a user
     */
    long countByUserIdAndIsCompleted(String userId, boolean isCompleted);
    
    /**
     * Check if user is enrolled in a course
     */
    boolean existsByUserIdAndCourseId(String userId, String courseId);
    
    /**
     * Find all enrollments with progress greater than specified percentage
     */
    List<Enrollment> findByUserIdAndProgressPercentageGreaterThanOrderByLastAccessedAtDesc(
            String userId, int progressPercentage);
    
    /**
     * Find recent enrollments (for dashboard)
     */
    List<Enrollment> findTop5ByUserIdOrderByLastAccessedAtDesc(String userId);

    /**
     * Delete all enrollments for a specific course
     */
    void deleteByCourseId(String courseId);
}
