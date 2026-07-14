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
@Document(collection = "coupons")
public class Coupon {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // e.g., "SAVE20", "SUMMER50"

    private Integer discountPercentage; // 1-100

    private Integer maxGlobalUsage; // Maximum total uses across all users

    @Builder.Default
    private Integer currentGlobalUsage = 0; // Current usage count

    private Integer minPurchaseAmount; // Minimum course price in rupees (optional)

    private LocalDateTime validFrom; // When coupon becomes active

    private LocalDateTime validUntil; // Expiry date (TTL index)

    @Builder.Default
    private Boolean isActive = true; // Admin can disable coupon

    private String createdBy; // Admin user ID who created this coupon

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
