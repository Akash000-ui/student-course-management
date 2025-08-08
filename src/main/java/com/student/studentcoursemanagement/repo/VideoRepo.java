package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.Video;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepo extends MongoRepository<Video , String> {
}
