package com.student.studentcoursemanagement.repo;

import java.time.LocalDateTime;
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
     * Find all enrollments for a specific course
     */
    List<Enrollment> findByCourseIdOrderByEnrolledAtDesc(String courseId);

    /**
     * Find all enrollments for a specific course (without ordering)
     */
    List<Enrollment> findByCourseId(String courseId);

    /**
     * Count total enrollments for a course
     */
    long countByCourseId(String courseId);

    /**
     * Count enrollments after a specific date
     */
    int countByEnrolledAtAfter(LocalDateTime date);

    /**
     * Count total enrollments for a user
     */
    long countByUserId(String userId);

    /**
     * Check if user is enrolled in a course
     */
    boolean existsByUserIdAndCourseId(String userId, String courseId);

    /**
     * Find recent enrollments (for dashboard)
     */
    List<Enrollment> findTop5ByUserIdOrderByLastAccessedAtDesc(String userId);

    /**
     * Delete all enrollments for a specific course
     */
    void deleteByCourseId(String courseId);
}
