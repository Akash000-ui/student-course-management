package com.student.studentcoursemanagement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "coupon_usage")
@CompoundIndex(def = "{'couponId': 1, 'userId': 1}", unique = true)
public class CouponUsage {

    @Id
    private String id;

    private String couponId; // Foreign key to Coupon

    private String userId; // Foreign key to User

    @Builder.Default
    private LocalDateTime usedAt = LocalDateTime.now();
}
