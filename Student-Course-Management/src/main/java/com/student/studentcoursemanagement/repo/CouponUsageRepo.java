package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.CouponUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CouponUsageRepo extends MongoRepository<CouponUsage, String> {

    // Check if user has already used a coupon
    Optional<CouponUsage> findByCouponIdAndUserId(String couponId, String userId);

    // Get all coupons used by a user
    List<CouponUsage> findByUserId(String userId);

    // Get all users who used a specific coupon
    List<CouponUsage> findByCouponId(String couponId);

    // Count total uses of a coupon
    long countByCouponId(String couponId);
}
