package com.student.studentcoursemanagement.controller;

import com.student.studentcoursemanagement.dto.AdminDashboardStatsDTO;
import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.model.Coupon;
import com.student.studentcoursemanagement.model.Order;
import com.student.studentcoursemanagement.model.PaymentStatus;
import com.student.studentcoursemanagement.repo.OrderRepo;
import com.student.studentcoursemanagement.service.AdminStatsService;
import com.student.studentcoursemanagement.service.AnalyticsService;
import com.student.studentcoursemanagement.service.CouponService;
import com.student.studentcoursemanagement.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminStatsService adminStatsService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private OrderRepo orderRepo;

    // ==================== LEGACY STATS ENDPOINT ====================

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

    // ==================== COUPON MANAGEMENT ====================

    /**
     * Create a new coupon (Admin only)
     */
    @PostMapping("/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(
            @RequestBody Coupon coupon,
            Authentication authentication) {

        String userId = authentication.getName();
        logger.info("Admin {} creating new coupon: {}", userId, coupon.getCode());

        try {
            ApiResponse<Coupon> response = couponService.createCoupon(coupon, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating coupon", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to create coupon: " + e.getMessage(), null,
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Get all coupons with usage stats (Admin only)
     */
    @GetMapping("/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {

        logger.info("Fetching all coupons for admin");

        try {
            ApiResponse<List<Coupon>> response = couponService.getAllCoupons();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching coupons", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch coupons", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Update coupon (Admin only)
     */
    @PutMapping("/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Coupon>> updateCoupon(
            @PathVariable String id,
            @RequestBody Coupon coupon,
            Authentication authentication) {
        String userId = authentication.getName();
        logger.info("Admin {} updating coupon: {}", userId, id);

        try {
            ApiResponse<Coupon> response = couponService.updateCoupon(id, coupon);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating coupon", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to update coupon: " + e.getMessage(), null,
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Delete coupon (Admin only)
     */
    @DeleteMapping("/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(
            @PathVariable String id,
            Authentication authentication) {

        String userId = authentication.getName();
        logger.info("Admin {} deleting coupon: {}", userId, id);

        try {
            ApiResponse<Void> response = couponService.deleteCoupon(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting coupon", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to delete coupon: " + e.getMessage(), null,
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Get most used coupons (Admin only)
     */
    @GetMapping("/coupons/stats/most-used")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMostUsedCoupons(
            @RequestParam(defaultValue = "10") int limit) {

        logger.info("Fetching top {} most used coupons", limit);

        try {
            List<Map<String, Object>> mostUsed = analyticsService.getMostUsedCoupons(limit);
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Most used coupons fetched", mostUsed, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching most used coupons", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch coupons", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // ==================== PAYMENT ANALYTICS ====================

    /**
     * Get dashboard summary (Admin only)
     */
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary() {

        logger.info("Fetching admin dashboard summary");

        try {
            Map<String, Object> summary = analyticsService.getDashboardSummary();
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Dashboard summary fetched", summary, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching dashboard summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch dashboard summary", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get revenue statistics (Admin only)
     */
    @GetMapping("/analytics/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueStats() {

        logger.info("Fetching revenue statistics");

        try {
            Map<String, Object> stats = analyticsService.getTotalRevenueStats();
            return ResponseEntity.ok(new ApiResponse<>(true, "Revenue stats fetched", stats, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching revenue stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch revenue stats", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get top selling courses (Admin only)
     */
    @GetMapping("/analytics/top-courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopCourses(
            @RequestParam(defaultValue = "10") int limit) {

        logger.info("Fetching top {} courses", limit);

        try {
            List<Map<String, Object>> topCourses = analyticsService.getTopSellingCourses(limit);
            return ResponseEntity.ok(new ApiResponse<>(true, "Top courses fetched", topCourses, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching top courses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch top courses", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get payment status distribution (Admin only)
     */
    @GetMapping("/analytics/payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPaymentStatusDistribution() {

        logger.info("Fetching payment status distribution");

        try {
            Map<String, Long> distribution = analyticsService.getPaymentStatusDistribution();
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment status distribution fetched", distribution,
                    HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching payment status distribution", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch distribution", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get coupon statistics (Admin only)
     */
    @GetMapping("/analytics/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCouponStats() {

        logger.info("Fetching coupon statistics");

        try {
            Map<String, Object> stats = analyticsService.getCouponUsageStats();
            return ResponseEntity.ok(new ApiResponse<>(true, "Coupon stats fetched", stats, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching coupon stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch coupon stats", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get payment failure statistics (Admin only)
     */
    @GetMapping("/analytics/failures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFailureStats() {

        logger.info("Fetching payment failure statistics");

        try {
            Map<String, Object> stats = analyticsService.getPaymentFailures();
            return ResponseEntity.ok(new ApiResponse<>(true, "Failure stats fetched", stats, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching failure stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch failure stats", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get daily revenue chart data (Admin only)
     */
    @GetMapping("/analytics/daily-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyRevenueChart(
            @RequestParam(defaultValue = "30") int days) {

        logger.info("Fetching daily revenue for {} days", days);

        try {
            List<Map<String, Object>> chartData = analyticsService.getDailyRevenueChart(days);
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Daily revenue chart fetched", chartData, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching daily revenue chart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch chart data", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // ==================== ORDER MANAGEMENT ====================

    /**
     * Get all orders (Admin only)
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders(
            @RequestParam(required = false) String paymentStatus) {

        logger.info("Admin fetching all orders with status: {}", paymentStatus);

        try {
            List<Order> orders;
            if (paymentStatus != null && !paymentStatus.isEmpty()) {
                try {
                    PaymentStatus status = PaymentStatus.valueOf(paymentStatus.toUpperCase());
                    orders = orderRepo.findByPaymentStatus(status);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse<>(false, "Invalid payment status: " + paymentStatus, null,
                                    HttpStatus.BAD_REQUEST.value()));
                }
            } else {
                orders = orderRepo.findAll();
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Orders fetched", orders, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch orders", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Process refund for an order (Admin only)
     */
    @PostMapping("/orders/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> refundOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String userId = authentication.getName();
        String reason = request.get("reason");
        logger.info("Admin {} processing refund for order: {} with reason: {}", userId, orderId, reason);

        try {
            ApiResponse<Void> response = paymentService.refundOrder(orderId, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing refund", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to process refund: " + e.getMessage(), null,
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Get order details (Admin only)
     */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> getOrderDetails(@PathVariable String orderId) {

        logger.info("Fetching details for order: {}", orderId);

        try {
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order != null) {
                return ResponseEntity
                        .ok(new ApiResponse<>(true, "Order details fetched", order, HttpStatus.OK.value()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Order not found", null, HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            logger.error("Error fetching order details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch order", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get orders by user (Admin only)
     */
    @GetMapping("/users/{userId}/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@PathVariable String userId) {

        logger.info("Fetching orders for user: {}", userId);

        try {
            List<Order> orders = orderRepo.findByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "User orders fetched", orders, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching user orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch user orders", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get orders by course (Admin only)
     */
    @GetMapping("/courses/{courseId}/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getCourseOrders(@PathVariable String courseId) {

        logger.info("Fetching orders for course: {}", courseId);

        try {
            List<Order> orders = orderRepo.findByCourseId(courseId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Course orders fetched", orders, HttpStatus.OK.value()));
        } catch (Exception e) {
            logger.error("Error fetching course orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch course orders", null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
