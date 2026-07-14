package com.student.studentcoursemanagement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @Indexed
    private String userId; // Foreign key to User

    @Indexed
    private String courseId; // Foreign key to Course

    private Integer originalPrice; // Price in rupees

    private Integer discountAmount; // Discount in rupees (0 if no coupon)

    private String couponCode; // Coupon code used (optional)

    private Integer finalAmount; // Price after discount

    // Payment Details from Razorpay
    @Indexed
    private String razorpayOrderId; // Razorpay order ID

    private String razorpayPaymentId; // Razorpay payment ID (set after verification)

    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.INITIATED;

    private String enrollmentId; // Foreign key to Enrollment (set after payment success)

    private String receiptUrl; // URL to PDF receipt

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt; // Set when payment is successfully completed

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", originalPrice=" + originalPrice +
                ", discountAmount=" + discountAmount +
                ", finalAmount=" + finalAmount +
                ", paymentStatus=" + paymentStatus +
                ", orderStatus=" + orderStatus +
                ", createdAt=" + createdAt +
                '}';
    }
}
