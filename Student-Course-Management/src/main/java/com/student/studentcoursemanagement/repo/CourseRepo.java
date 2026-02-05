package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.Course;
import com.student.studentcoursemanagement.model.DifficultyLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends MongoRepository<Course, String> {

    // Find courses by category ID
    List<Course> findByCategoryId(String categoryId);

    // Count courses by category ID
    long countByCategoryId(String categoryId);

    // Find courses by difficulty
    List<Course> findByDifficulty(DifficultyLevel difficulty);

    // Find courses by category ID and difficulty
    List<Course> findByCategoryIdAndDifficulty(String categoryId, DifficultyLevel difficulty);

    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<Course> findByTitleContainingIgnoreCase(String title);

    // Find courses by title containing search term and category ID
    @Query("{'title': {$regex: ?0, $options: 'i'}, 'categoryId': ?1}")
    List<Course> findByTitleContainingIgnoreCaseAndCategoryId(String title, String categoryId);

    // Find courses by title containing search term and difficulty
    @Query("{'title': {$regex: ?0, $options: 'i'}, 'difficulty': ?1}")
    List<Course> findByTitleContainingIgnoreCaseAndDifficulty(String title, DifficultyLevel difficulty);

    // Find courses by title containing search term, category ID and difficulty
    @Query("{'title': {$regex: ?0, $options: 'i'}, 'categoryId': ?1, 'difficulty': ?2}")
    List<Course> findByTitleContainingIgnoreCaseAndCategoryIdAndDifficulty(
            String title, String categoryId, DifficultyLevel difficulty);
}
