package com.student.studentcoursemanagement.controller;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.model.Order;
import com.student.studentcoursemanagement.service.PaymentService;
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

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * Initiate payment for a course
     * POST /api/payments/initiate
     * Request body: { "courseId": "...", "couponCode": "..." (optional) }
     */
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiatePayment(
            @Valid @RequestBody Map<String, String> request,
            Authentication authentication) {

        String userId = authentication.getName();
        String courseId = request.get("courseId");
        String couponCode = request.get("couponCode");

        logger.info("Payment initiation request - userId: {}, courseId: {}, couponCode: {}",
                userId, courseId, couponCode);

        ApiResponse<Map<String, Object>> response = paymentService.initiatePayment(userId, courseId, couponCode);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Verify payment and create enrollment
     * POST /api/payments/verify
     * Request body: {
     * "razorpayOrderId": "...",
     * "razorpayPaymentId": "...",
     * "razorpaySignature": "..."
     * }
     */
    @PostMapping("/verify")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPayment(
            @Valid @RequestBody Map<String, String> request,
            Authentication authentication) {

        String razorpayOrderId = request.get("razorpayOrderId");
        String razorpayPaymentId = request.get("razorpayPaymentId");
        String razorpaySignature = request.get("razorpaySignature");

        logger.info("Payment verification request - orderId: {}, paymentId: {}",
                razorpayOrderId, razorpayPaymentId);

        ApiResponse<Map<String, Object>> response = paymentService.verifyPayment(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Get all orders for current user
     * GET /api/payments/orders
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(
            Authentication authentication) {

        String userId = authentication.getName();

        logger.info("Get user orders request - userId: {}", userId);

        ApiResponse<List<Order>> response = paymentService.getUserOrders(userId);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Get specific order details
     * GET /api/payments/orders/{orderId}
     */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> getOrderDetails(
            @PathVariable String orderId,
            Authentication authentication) {

        String userId = authentication.getName();

        logger.info("Get order details request - orderId: {}, userId: {}", orderId, userId);

        ApiResponse<Order> response = paymentService.getOrderDetails(orderId);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Refund an order (Admin only)
     * POST /api/payments/refund/{orderId}
     * Request body: { "reason": "..." }
     */
    @PostMapping("/refund/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> refundOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {

        String reason = request.get("reason");

        logger.info("Refund request - orderId: {}, reason: {}", orderId, reason);

        ApiResponse<Void> response = paymentService.refundOrder(orderId, reason);
        int statusCode = response.getStatusCode() > 0 ? response.getStatusCode() : 200;

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Get payment stats (Admin only)
     * GET /api/payments/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStats() {

        logger.info("Get payment stats request");

        // This will be implemented in the next iteration
        Map<String, Object> stats = Map.of(
                "totalOrders", 0,
                "totalRevenue", 0.0,
                "successfulPayments", 0,
                "failedPayments", 0,
                "message", "Stats will be calculated from payment service");

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                true,
                "Payment statistics retrieved",
                stats);
        response.setStatusCode(200);

        return ResponseEntity.ok(response);
    }
}
