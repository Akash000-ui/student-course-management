package com.student.studentcoursemanagement.controller;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.CategoryRequestDTO;
import com.student.studentcoursemanagement.dto.CategoryResponseDTO;
import com.student.studentcoursemanagement.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    /**
     * Get all active categories (public access)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories() {
        logger.info("Request to get all active categories");
        ApiResponse<List<CategoryResponseDTO>> response = categoryService.getAllCategories();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Get all categories including inactive ones (admin only)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategoriesForAdmin() {
        logger.info("Admin request to get all categories");
        ApiResponse<List<CategoryResponseDTO>> response = categoryService.getAllCategoriesForAdmin();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable String id) {
        logger.info("Request to get category with ID: {}", id);
        ApiResponse<CategoryResponseDTO> response = categoryService.getCategoryById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Create a new category (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(
            @Valid @RequestBody CategoryRequestDTO request) {
        logger.info("Admin request to create category: {}", request.getName());
        ApiResponse<CategoryResponseDTO> response = categoryService.createCategory(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Update a category (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequestDTO request) {
        logger.info("Admin request to update category with ID: {}", id);
        ApiResponse<CategoryResponseDTO> response = categoryService.updateCategory(id, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Delete (soft delete) a category (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
        logger.info("Admin request to delete category with ID: {}", id);
        ApiResponse<Void> response = categoryService.deleteCategory(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Permanently delete a category (admin only)
     */
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteCategory(@PathVariable String id) {
        logger.info("Admin request to permanently delete category with ID: {}", id);
        ApiResponse<Void> response = categoryService.permanentlyDeleteCategory(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
