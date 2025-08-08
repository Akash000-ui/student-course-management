package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepo extends MongoRepository<Course , String> {
}
