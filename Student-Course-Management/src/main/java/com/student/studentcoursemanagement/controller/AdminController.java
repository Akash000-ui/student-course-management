package com.student.studentcoursemanagement.controller;

import com.student.studentcoursemanagement.dto.AdminDashboardStatsDTO;
import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.service.AdminStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminStatsService adminStatsService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDTO>> getAdminStats() {
        logger.info("Fetching admin dashboard statistics");

        try {
            AdminDashboardStatsDTO stats = adminStatsService.getAdminDashboardStats();

            ApiResponse<AdminDashboardStatsDTO> response = new ApiResponse<>(
                    true,
                    "Statistics retrieved successfully",
                    stats);
            response.setStatusCode(200);

            logger.info("Admin statistics retrieved successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching admin statistics: {}", e.getMessage());
            ApiResponse<AdminDashboardStatsDTO> response = new ApiResponse<>(
                    false,
                    "Failed to retrieve statistics: " + e.getMessage(),
                    null);
            response.setStatusCode(500);
            return ResponseEntity.status(500).body(response);
        }
    }
}
