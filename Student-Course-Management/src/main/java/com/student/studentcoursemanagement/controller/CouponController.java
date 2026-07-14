package com.student.studentcoursemanagement.controller;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.model.Coupon;
import com.student.studentcoursemanagement.service.CouponService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private static final Logger logger = LoggerFactory.getLogger(CouponController.class);

    @Autowired
    private CouponService couponService;

    /**
     * Create a new coupon (Admin only)
     * POST /api/coupons
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(
            @Valid @RequestBody Coupon coupon,
            Authentication authentication) {

        logger.info("Create coupon request for code: {}", coupon.getCode());

        String adminUserId = authentication.getName();
        ApiResponse<Coupon> response = couponService.createCoupon(coupon, adminUserId);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 201;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Get all coupons (Admin only)
     * GET /api/coupons
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {

        logger.info("Get all coupons request");

        ApiResponse<List<Coupon>> response = couponService.getAllCoupons();
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Get active coupons (Users can see available coupons)
     * GET /api/coupons/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Coupon>>> getActiveCoupons() {

        logger.info("Get active coupons request");

        List<Coupon> coupons = couponService.getActiveCoupons();
        ApiResponse<List<Coupon>> response = new ApiResponse<>(
                true,
                "Active coupons retrieved successfully",
                coupons);
        response.setStatusCode(200);

        return ResponseEntity.ok(response);
    }

    /**
     * Validate coupon for purchase
     * GET /api/coupons/validate/{code}?userId={userId}&coursePrice={price}
     */
    @GetMapping("/validate/{code}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCoupon(
            @PathVariable String code,
            @RequestParam String userId,
            @RequestParam Integer coursePrice) {

        logger.info("Validate coupon request - code: {}, userId: {}, price: {}", code, userId, coursePrice);

        Map<String, Object> validationResult = couponService.validateCoupon(code, userId, coursePrice);
        boolean isValid = (Boolean) validationResult.get("valid");

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                isValid,
                (String) validationResult.get("message"),
                validationResult);
        response.setStatusCode(isValid ? 200 : 400);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Get single coupon by ID (Admin only)
     * GET /api/coupons/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Coupon>> getCouponById(@PathVariable String id) {

        logger.info("Get coupon by ID: {}", id);

        ApiResponse<Coupon> response = couponService.getCouponById(id);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Update coupon (Admin only)
     * PUT /api/coupons/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Coupon>> updateCoupon(
            @PathVariable String id,
            @Valid @RequestBody Coupon coupon) {

        logger.info("Update coupon request - ID: {}", id);

        ApiResponse<Coupon> response = couponService.updateCoupon(id, coupon);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Delete/Disable coupon (Admin only)
     * DELETE /api/coupons/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable String id) {

        logger.info("Delete coupon request - ID: {}", id);

        ApiResponse<Void> response = couponService.deleteCoupon(id);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Get user's coupon history
     * GET /api/coupons/user/{userId}/history
     */
    @GetMapping("/user/{userId}/history")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserCouponHistory(
            @PathVariable String userId) {

        logger.info("Get user coupon history - userId: {}", userId);

        var history = couponService.getUserCouponHistory(userId).stream()
                .map(usage -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("couponId", usage.getCouponId());
                    map.put("usedAt", usage.getUsedAt());
                    return map;
                })
                .toList();

        ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                true,
                "Coupon history retrieved successfully",
                history);
        response.setStatusCode(200);

        return ResponseEntity.ok(response);
    }

    /**
     * Get usage statistics for a coupon (Admin only)
     * GET /api/coupons/{id}/usage-stats
     */
    @GetMapping("/{id}/usage-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCouponUsageStats(
            @PathVariable String id) {

        logger.info("Get coupon usage stats - couponId: {}", id);

        var usageHistory = couponService.getCouponUsageHistory(id).stream()
                .map(usage -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", usage.getUserId());
                    map.put("usedAt", usage.getUsedAt());
                    return map;
                })
                .toList();

        Map<String, Object> stats = new HashMap<>();
        stats.put("couponId", id);
        stats.put("totalUsages", usageHistory.size());
        stats.put("usageDetails", usageHistory);

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                true,
                "Coupon usage stats retrieved successfully",
                stats);
        response.setStatusCode(200);

        return ResponseEntity.ok(response);
    }
}
