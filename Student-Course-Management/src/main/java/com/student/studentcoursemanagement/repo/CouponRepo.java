package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface CouponRepo extends MongoRepository<Coupon, String> {

    // Find coupon by code
    Optional<Coupon> findByCode(String code);

    // Find active coupons that are valid now
    List<Coupon> findByIsActiveTrueAndValidFromLessThanEqualAndValidUntilGreaterThanEqual(LocalDateTime from,
            LocalDateTime until);

    // Count active coupons
    long countByIsActiveTrue();
}
