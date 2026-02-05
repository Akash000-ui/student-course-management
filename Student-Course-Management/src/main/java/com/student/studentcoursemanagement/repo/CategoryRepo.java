package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepo extends MongoRepository<Category, String> {

    List<Category> findByActiveTrue();

    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}
