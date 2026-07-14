package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.CategoryRequestDTO;
import com.student.studentcoursemanagement.dto.CategoryResponseDTO;
import com.student.studentcoursemanagement.model.Category;
import com.student.studentcoursemanagement.repo.CategoryRepo;
import com.student.studentcoursemanagement.repo.CourseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepo categoryRepository;

    @Autowired
    private CourseRepo courseRepository;

    /**
     * Get all active categories
     */
    public ApiResponse<List<CategoryResponseDTO>> getAllCategories() {
        try {
            logger.info("Fetching all active categories");
            List<Category> categories = categoryRepository.findByActiveTrue();
            List<CategoryResponseDTO> responseDTOs = categories.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            logger.info("Retrieved {} active categories", responseDTOs.size());
            return new ApiResponse<>(true, "Categories retrieved successfully", responseDTOs, 200);
        } catch (Exception e) {
            logger.error("Error fetching categories", e);
            return new ApiResponse<>(false, "Failed to fetch categories: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Get all categories including inactive ones (admin only)
     */
    public ApiResponse<List<CategoryResponseDTO>> getAllCategoriesForAdmin() {
        try {
            logger.info("Fetching all categories for admin");
            List<Category> categories = categoryRepository.findAll();
            List<CategoryResponseDTO> responseDTOs = categories.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            logger.info("Retrieved {} total categories", responseDTOs.size());
            return new ApiResponse<>(true, "Categories retrieved successfully", responseDTOs, 200);
        } catch (Exception e) {
            logger.error("Error fetching categories", e);
            return new ApiResponse<>(false, "Failed to fetch categories: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Get category by ID
     */
    public ApiResponse<CategoryResponseDTO> getCategoryById(String id) {
        try {
            logger.info("Fetching category with ID: {}", id);
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

            return new ApiResponse<>(true, "Category retrieved successfully", convertToResponseDTO(category), 200);
        } catch (Exception e) {
            logger.error("Error fetching category", e);
            return new ApiResponse<>(false, e.getMessage(), null, 404);
        }
    }

    /**
     * Create a new category
     */
    public ApiResponse<CategoryResponseDTO> createCategory(CategoryRequestDTO request) {
        try {
            logger.info("Creating new category: {}", request.getName());

            // Check if category with same name already exists
            if (categoryRepository.existsByName(request.getName())) {
                logger.error("Category with name '{}' already exists", request.getName());
                return new ApiResponse<>(false, "Category with this name already exists", null, 400);
            }

            Category category = Category.builder()
                    .name(request.getName().trim())
                    .description(request.getDescription() != null ? request.getDescription().trim() : null)
                    .iconUrl(request.getIconUrl())
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Category savedCategory = categoryRepository.save(category);
            logger.info("Category created successfully with ID: {}", savedCategory.getId());

            return new ApiResponse<>(true, "Category created successfully", convertToResponseDTO(savedCategory), 201);
        } catch (Exception e) {
            logger.error("Error creating category", e);
            return new ApiResponse<>(false, "Failed to create category: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Update an existing category
     */
    public ApiResponse<CategoryResponseDTO> updateCategory(String id, CategoryRequestDTO request) {
        try {
            logger.info("Updating category with ID: {}", id);

            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

            // Check if name is being changed and if new name already exists
            if (!category.getName().equals(request.getName()) &&
                    categoryRepository.existsByName(request.getName())) {
                logger.error("Category with name '{}' already exists", request.getName());
                return new ApiResponse<>(false, "Category with this name already exists", null, 400);
            }

            category.setName(request.getName().trim());
            category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            category.setIconUrl(request.getIconUrl());
            category.setUpdatedAt(LocalDateTime.now());

            Category updatedCategory = categoryRepository.save(category);
            logger.info("Category updated successfully with ID: {}", updatedCategory.getId());

            return new ApiResponse<>(true, "Category updated successfully", convertToResponseDTO(updatedCategory), 200);
        } catch (Exception e) {
            logger.error("Error updating category", e);
            return new ApiResponse<>(false, e.getMessage(), null, 500);
        }
    }

    /**
     * Delete (soft delete) a category
     */
    public ApiResponse<Void> deleteCategory(String id) {
        try {
            logger.info("Deleting category with ID: {}", id);

            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

            // Check if any courses are using this category
            long courseCount = courseRepository.countByCategoryId(id);
            if (courseCount > 0) {
                logger.error("Cannot delete category. {} courses are using this category", courseCount);
                return new ApiResponse<>(false,
                        "Cannot delete category. " + courseCount
                                + " course(s) are using this category. Please reassign or delete those courses first.",
                        null, 400);
            }

            // Soft delete
            category.setActive(false);
            category.setUpdatedAt(LocalDateTime.now());
            categoryRepository.save(category);

            logger.info("Category soft deleted successfully with ID: {}", id);
            return new ApiResponse<>(true, "Category deleted successfully", null, 200);
        } catch (Exception e) {
            logger.error("Error deleting category", e);
            return new ApiResponse<>(false, e.getMessage(), null, 500);
        }
    }

    /**
     * Permanently delete a category
     */
    public ApiResponse<Void> permanentlyDeleteCategory(String id) {
        try {
            logger.info("Permanently deleting category with ID: {}", id);

            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

            // Check if any courses are using this category
            long courseCount = courseRepository.countByCategoryId(id);
            if (courseCount > 0) {
                logger.error("Cannot delete category. {} courses are using this category", courseCount);
                return new ApiResponse<>(false,
                        "Cannot delete category. " + courseCount
                                + " course(s) are using this category. Please reassign or delete those courses first.",
                        null, 400);
            }

            categoryRepository.deleteById(id);
            logger.info("Category permanently deleted with ID: {}", id);

            return new ApiResponse<>(true, "Category permanently deleted successfully", null, 200);
        } catch (Exception e) {
            logger.error("Error permanently deleting category", e);
            return new ApiResponse<>(false, e.getMessage(), null, 500);
        }
    }

    /**
     * Convert Category entity to CategoryResponseDTO
     */
    private CategoryResponseDTO convertToResponseDTO(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
