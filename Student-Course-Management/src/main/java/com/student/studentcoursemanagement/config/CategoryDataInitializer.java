package com.student.studentcoursemanagement.config;

import com.student.studentcoursemanagement.model.Category;
import com.student.studentcoursemanagement.repo.CategoryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class CategoryDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDataInitializer.class);

    @Autowired
    private CategoryRepo categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeCategories();
    }

    private void initializeCategories() {
        try {
            // Check if categories already exist
            long count = categoryRepository.count();
            if (count > 0) {
                logger.info("Categories already initialized. Skipping initialization.");
                return;
            }

            logger.info("Initializing default categories...");

            List<Category> defaultCategories = Arrays.asList(
                    Category.builder()
                            .name("Programming")
                            .description("Learn programming languages and software development")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Category.builder()
                            .name("Data Science")
                            .description("Explore data analysis, machine learning, and AI")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Category.builder()
                            .name("Web Development")
                            .description("Build modern websites and web applications")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Category.builder()
                            .name("Mobile Development")
                            .description("Create mobile apps for iOS and Android")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Category.builder()
                            .name("Cloud Computing")
                            .description("Master cloud platforms and DevOps")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Category.builder()
                            .name("Cybersecurity")
                            .description("Learn security best practices and ethical hacking")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build());

            categoryRepository.saveAll(defaultCategories);
            logger.info("Successfully initialized {} default categories", defaultCategories.size());

        } catch (Exception e) {
            logger.error("Error initializing categories", e);
        }
    }
}
