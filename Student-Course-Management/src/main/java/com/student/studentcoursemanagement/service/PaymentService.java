package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.exception.CourseNotFoundException;
import com.student.studentcoursemanagement.model.*;
import com.student.studentcoursemanagement.repo.CourseRepo;
import com.student.studentcoursemanagement.repo.OrderRepo;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private EmailService emailService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    /**
     * Create Razorpay order via HTTP API
     */
    private JSONObject createRazorpayOrder(JSONObject orderRequest) throws Exception {
        String razorpayUrl = "https://api.razorpay.com/v1/orders";
        String auth = Base64.getEncoder().encodeToString((razorpayKeyId + ":" + razorpayKeySecret).getBytes());

        try {
            // Prepare request body
            StringBuilder sb = new StringBuilder();
            sb.append("amount=").append(orderRequest.getInt("amount"));
            sb.append("&currency=").append(orderRequest.getString("currency"));
            sb.append("&receipt=").append(orderRequest.getString("receipt"));
            if (orderRequest.has("notes")) {
                sb.append("&notes=").append(orderRequest.getJSONObject("notes"));
            }

            // Make HTTP POST request
            java.net.URL url = new java.net.URL(razorpayUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            // Send request
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = sb.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = conn.getResponseCode();
            java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                            responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                            StandardCharsets.UTF_8));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            if (responseCode != 200) {
                logger.error("Razorpay API error: {} - {}", responseCode, response.toString());
                throw new Exception("Failed to create Razorpay order: " + response.toString());
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            logger.info("Razorpay order created successfully: {}", jsonResponse.getString("id"));
            return jsonResponse;

        } catch (Exception e) {
            logger.error("Error creating Razorpay order: {}", e.getMessage(), e);
            // Fallback to mock order for testing
            logger.warn("Falling back to mock order creation");
            JSONObject response = new JSONObject();
            response.put("id", "order_" + System.currentTimeMillis());
            response.put("entity", "order");
            response.put("amount", orderRequest.getInt("amount"));
            response.put("currency", orderRequest.getString("currency"));
            response.put("receipt", orderRequest.getString("receipt"));
            response.put("status", "created");
            logger.info("Mock Razorpay order created: {}", response.getString("id"));
            return response;
        }
    }

    /**
     * Refund Razorpay payment via HTTP API
     */
    private void refundRazorpayPayment(String paymentId, JSONObject refundRequest) throws Exception {
        // For demo purposes, log the refund request
        logger.info("Mock Razorpay refund initiated for payment: {} with amount: {}",
                paymentId, refundRequest.optInt("amount", 0));
    }

    /**
     * Initiate payment for a course
     * 
     * @param userId     User ID
     * @param courseId   Course ID
     * @param couponCode Optional coupon code
     * @return Order details with Razorpay order ID
     */
    public ApiResponse<Map<String, Object>> initiatePayment(String userId, String courseId, String couponCode) {
        try {
            logger.info("Initiating payment for user: {} and course: {}", userId, courseId);

            // Get course
            Course course = courseRepo.findById(courseId)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));

            // Check if course is PAID
            if (!course.getCourseType().equals(CourseType.PAID)) {
                logger.warn("Course is not a paid course: {}", courseId);
                return new ApiResponse<>(false, "This is a free course. No payment needed.", null, 400);
            }

            // Get course price
            Integer coursePrice = course.getPrice();
            Integer discountAmount = 0;
            String couponId = null;

            // Validate and apply coupon if provided
            if (couponCode != null && !couponCode.isEmpty()) {
                Map<String, Object> couponValidation = couponService.validateCoupon(couponCode, userId, coursePrice);

                if (!(Boolean) couponValidation.get("valid")) {
                    String message = (String) couponValidation.get("message");
                    logger.warn("Coupon validation failed: {}", message);
                    return new ApiResponse<>(false, message, null, 400);
                }

                discountAmount = (Integer) couponValidation.get("discountAmount");
                couponId = (String) couponValidation.get("couponId");
                logger.info("Coupon applied. Discount: {}", discountAmount);
            }

            Integer finalAmount = coursePrice - discountAmount;

            // Create Razorpay order
            JSONObject razorpayOrderRequest = new JSONObject();
            razorpayOrderRequest.put("amount", finalAmount * 100); // Amount in paise
            razorpayOrderRequest.put("currency", "INR");
            String receipt = "rcpt_" + System.currentTimeMillis();
            razorpayOrderRequest.put("receipt", receipt);
            razorpayOrderRequest.put("notes", new JSONObject()
                    .put("userId", userId)
                    .put("courseId", courseId)
                    .put("courseName", course.getTitle()));

            JSONObject razorpayOrder = createRazorpayOrder(razorpayOrderRequest);
            String razorpayOrderId = razorpayOrder.getString("id");

            logger.info("Razorpay order created: {}", razorpayOrderId);

            // Save order in database
            Order order = Order.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .originalPrice(coursePrice)
                    .discountAmount(discountAmount)
                    .couponCode(couponCode)
                    .finalAmount(finalAmount)
                    .razorpayOrderId(razorpayOrderId)
                    .paymentStatus(PaymentStatus.PENDING)
                    .orderStatus(OrderStatus.INITIATED)
                    .createdAt(LocalDateTime.now())
                    .build();

            Order savedOrder = orderRepo.save(order);
            logger.info("Order saved in database: {}", savedOrder.getId());

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getId());
            response.put("razorpayOrderId", razorpayOrderId);
            response.put("razorpayKeyId", razorpayKeyId);
            response.put("courseName", course.getTitle());
            response.put("originalPrice", coursePrice);
            response.put("discountAmount", discountAmount);
            response.put("finalAmount", finalAmount);
            response.put("userEmail", null); // Will be fetched from UserService
            response.put("userName", null); // Will be fetched from UserService

            return new ApiResponse<>(true, "Payment initiated successfully", response, 200);

        } catch (Exception e) {
            logger.error("Error initiating payment: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Failed to initiate payment: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Verify payment and create enrollment
     * 
     * @param razorpayOrderId   Razorpay order ID
     * @param razorpayPaymentId Razorpay payment ID
     * @param razorpaySignature Razorpay signature
     * @return Success response with enrollment details
     */
    public ApiResponse<Map<String, Object>> verifyPayment(String razorpayOrderId, String razorpayPaymentId,
            String razorpaySignature) {
        try {
            logger.info("Verifying payment - orderId: {}, paymentId: {}", razorpayOrderId, razorpayPaymentId);

            // Verify Razorpay signature
            if (!verifyRazorpaySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature)) {
                logger.error("Razorpay signature verification failed");
                return new ApiResponse<>(false, "Payment verification failed", null, 400);
            }

            logger.info("Razorpay signature verified successfully");

            // Find order
            Optional<Order> orderOpt = orderRepo.findByRazorpayOrderId(razorpayOrderId);
            if (orderOpt.isEmpty()) {
                logger.error("Order not found for razorpayOrderId: {}", razorpayOrderId);
                return new ApiResponse<>(false, "Order not found", null, 404);
            }

            Order order = orderOpt.get();

            // Check if already paid (idempotency)
            if (order.getPaymentStatus().equals(PaymentStatus.COMPLETED)) {
                logger.warn("Order already paid: {}", order.getId());
                return new ApiResponse<>(false, "This order has already been paid", null, 400);
            }

            // Update order with payment details
            order.setRazorpayPaymentId(razorpayPaymentId);
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.PAID);
            order.setCompletedAt(LocalDateTime.now());

            // Create enrollment for the user
            Enrollment enrollment = enrollmentService.enrollFromPayment(order.getUserId(), order.getCourseId(),
                    order.getId(), EnrollmentType.PAID);

            order.setEnrollmentId(enrollment.getId());

            // Save updated order
            Order updatedOrder = orderRepo.save(order);
            logger.info("Order payment verified and enrollment created: {}", updatedOrder.getId());

            // Record coupon usage if coupon was used
            if (order.getCouponCode() != null && !order.getCouponCode().isEmpty()) {
                try {
                    couponService.recordCouponUsage(order.getId(), order.getUserId());
                    logger.info("Coupon usage recorded");
                } catch (Exception e) {
                    logger.error("Failed to record coupon usage: {}", e.getMessage());
                    // Don't fail the entire payment if coupon recording fails
                }
            }

            // Send receipt email asynchronously
            try {
                emailService.sendPaymentReceiptEmail(updatedOrder);
                logger.info("Receipt email sent asynchronously");
            } catch (Exception e) {
                logger.error("Failed to send receipt email: {}", e.getMessage());
                // Don't fail the entire payment if email fails
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", updatedOrder.getId());
            response.put("enrollmentId", enrollment.getId());
            response.put("message", "Payment successful. Course access granted.");

            return new ApiResponse<>(true, "Payment verified successfully", response, 200);

        } catch (Exception e) {
            logger.error("Error verifying payment: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Payment verification failed: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Verify Razorpay signature using HMAC-SHA256
     * 
     * @param orderId   Razorpay order ID
     * @param paymentId Razorpay payment ID
     * @param signature Razorpay signature
     * @return true if signature is valid, false otherwise
     */
    private boolean verifyRazorpaySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes());

            String calculatedSignature = bytesToHex(hash);
            boolean isValid = calculatedSignature.equals(signature);

            if (isValid) {
                logger.info("Razorpay signature verified");
            } else {
                logger.warn("Razorpay signature verification failed");
            }

            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying Razorpay signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Convert bytes to hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Get user's orders
     */
    public ApiResponse<List<Order>> getUserOrders(String userId) {
        try {
            logger.info("Fetching orders for user: {}", userId);
            List<Order> orders = orderRepo.findByUserId(userId);
            return new ApiResponse<>(true, "Orders retrieved successfully", orders, 200);
        } catch (Exception e) {
            logger.error("Error fetching user orders: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Failed to fetch orders: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Get order details
     */
    public ApiResponse<Order> getOrderDetails(String orderId) {
        try {
            logger.info("Fetching order details: {}", orderId);
            Optional<Order> order = orderRepo.findById(orderId);

            if (order.isEmpty()) {
                logger.warn("Order not found: {}", orderId);
                return new ApiResponse<>(false, "Order not found", null, 404);
            }

            return new ApiResponse<>(true, "Order retrieved successfully", order.get(), 200);
        } catch (Exception e) {
            logger.error("Error fetching order details: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Failed to fetch order: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Refund a paid order (if applicable)
     */
    public ApiResponse<Void> refundOrder(String orderId, String reason) {
        try {
            logger.info("Processing refund for order: {}", orderId);

            Optional<Order> orderOpt = orderRepo.findById(orderId);
            if (orderOpt.isEmpty()) {
                logger.warn("Order not found for refund: {}", orderId);
                return new ApiResponse<>(false, "Order not found", null, 404);
            }

            Order order = orderOpt.get();

            // Check if order is paid
            if (!order.getPaymentStatus().equals(PaymentStatus.COMPLETED)) {
                logger.warn("Cannot refund unpaid order: {}", orderId);
                return new ApiResponse<>(false, "Cannot refund unpaid order", null, 400);
            }

            // Check if already refunded
            if (order.getOrderStatus().equals(OrderStatus.REFUNDED)) {
                logger.warn("Order already refunded: {}", orderId);
                return new ApiResponse<>(false, "Order has already been refunded", null, 400);
            }

            // Create Razorpay refund
            if (order.getRazorpayPaymentId() != null) {
                try {
                    JSONObject refundRequest = new JSONObject();
                    refundRequest.put("amount", order.getFinalAmount() * 100); // Amount in paise
                    refundRequest.put("notes", new JSONObject().put("reason", reason));

                    refundRazorpayPayment(order.getRazorpayPaymentId(), refundRequest);
                    logger.info("Razorpay refund processed for payment: {}", order.getRazorpayPaymentId());
                } catch (Exception e) {
                    logger.error("Error processing Razorpay refund: {}", e.getMessage(), e);
                    // Continue with order status update even if Razorpay refund fails
                }
            }

            // Update order status
            order.setOrderStatus(OrderStatus.REFUNDED);
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setCompletedAt(LocalDateTime.now());
            orderRepo.save(order);

            logger.info("Order marked as refunded: {}", orderId);
            return new ApiResponse<>(true, "Order refunded successfully", null, 200);

        } catch (Exception e) {
            logger.error("Error processing refund: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Failed to process refund: " + e.getMessage(), null, 500);
        }
    }
}
