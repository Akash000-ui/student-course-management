package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.Course;
import com.student.studentcoursemanagement.model.CourseCategory;
import com.student.studentcoursemanagement.model.DifficultyLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends MongoRepository<Course, String> {
    
    // Find courses by category
    List<Course> findByCategory(CourseCategory category);
    
    // Find courses by difficulty
    List<Course> findByDifficulty(DifficultyLevel difficulty);
    
    // Find courses by category and difficulty
    List<Course> findByCategoryAndDifficulty(CourseCategory category, DifficultyLevel difficulty);

    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<Course> findByTitleContainingIgnoreCase(String title);
    
    // Find courses by title containing search term and category
    @Query("{'title': {$regex: ?0, $options: 'i'}, 'category': ?1}")
    List<Course> findByTitleContainingIgnoreCaseAndCategory(String title, CourseCategory category);
    
    // Find courses by title containing search term and difficulty
    @Query("{'title': {$regex: ?0, $options: 'i'}, 'difficulty': ?1}")
    List<Course> findByTitleContainingIgnoreCaseAndDifficulty(String title, DifficultyLevel difficulty);
    
    // Find courses by title containing search term, category and difficulty
    @Query("{'title': {$regex: ?0, $options: 'i'}, 'category': ?1, 'difficulty': ?2}")
    List<Course> findByTitleContainingIgnoreCaseAndCategoryAndDifficulty(
            String title, CourseCategory category, DifficultyLevel difficulty);
            
    // Find courses by created by user
//    List<Course> findByCreatedBy(String createdBy);
}
