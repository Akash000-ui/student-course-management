package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.exception.InvalidCourseDataException;
import com.student.studentcoursemanagement.model.Coupon;
import com.student.studentcoursemanagement.model.CouponUsage;
import com.student.studentcoursemanagement.repo.CouponRepo;
import com.student.studentcoursemanagement.repo.CouponUsageRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CouponService {

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    @Autowired
    private CouponRepo couponRepo;

    @Autowired
    private CouponUsageRepo couponUsageRepo;

    /**
     * Create a new coupon (Admin only)
     */
    public ApiResponse<Coupon> createCoupon(Coupon couponRequest, String adminUserId) {
        try {
            logger.info("Creating new coupon with code: {}", couponRequest.getCode());

            // Check if coupon code already exists
            Optional<Coupon> existingCoupon = couponRepo.findByCode(couponRequest.getCode());
            if (existingCoupon.isPresent()) {
                logger.warn("Coupon code already exists: {}", couponRequest.getCode());
                return new ApiResponse<>(false, "Coupon code already exists", null, 400);
            }

            // Set admin user and timestamps
            couponRequest.setCreatedBy(adminUserId);
            couponRequest.setCreatedAt(LocalDateTime.now());
            couponRequest.setUpdatedAt(LocalDateTime.now());
            couponRequest.setCurrentGlobalUsage(0);

            Coupon savedCoupon = couponRepo.save(couponRequest);
            logger.info("Coupon created successfully with code: {}", savedCoupon.getCode());

            return new ApiResponse<>(true, "Coupon created successfully", savedCoupon, 201);
        } catch (Exception e) {
            logger.error("Error creating coupon: {}", e.getMessage(), e);
            throw new InvalidCourseDataException("Failed to create coupon: " + e.getMessage());
        }
    }

    /**
     * Get all coupons (Admin only)
     */
    public ApiResponse<List<Coupon>> getAllCoupons() {
        try {
            logger.info("Fetching all coupons");
            List<Coupon> coupons = couponRepo.findAll();
            return new ApiResponse<>(true, "Coupons retrieved successfully", coupons, 200);
        } catch (Exception e) {
            logger.error("Error fetching coupons: {}", e.getMessage(), e);
            throw new InvalidCourseDataException("Failed to fetch coupons: " + e.getMessage());
        }
    }

    /**
     * Get coupon by ID
     */
    public ApiResponse<Coupon> getCouponById(String id) {
        try {
            logger.info("Fetching coupon with ID: {}", id);
            Optional<Coupon> coupon = couponRepo.findById(id);

            if (coupon.isEmpty()) {
                logger.warn("Coupon not found with ID: {}", id);
                return new ApiResponse<>(false, "Coupon not found", null, 404);
            }

            return new ApiResponse<>(true, "Coupon retrieved successfully", coupon.get(), 200);
        } catch (Exception e) {
            logger.error("Error fetching coupon: {}", e.getMessage(), e);
            throw new InvalidCourseDataException("Failed to fetch coupon: " + e.getMessage());
        }
    }

    /**
     * Update coupon (Admin only)
     */
    public ApiResponse<Coupon> updateCoupon(String id, Coupon couponRequest) {
        try {
            logger.info("Updating coupon with ID: {}", id);

            Optional<Coupon> existingCoupon = couponRepo.findById(id);
            if (existingCoupon.isEmpty()) {
                logger.warn("Coupon not found for update: {}", id);
                return new ApiResponse<>(false, "Coupon not found", null, 404);
            }

            Coupon coupon = existingCoupon.get();

            // Update fields
            if (couponRequest.getDiscountPercentage() != null) {
                coupon.setDiscountPercentage(couponRequest.getDiscountPercentage());
            }
            if (couponRequest.getMaxGlobalUsage() != null) {
                coupon.setMaxGlobalUsage(couponRequest.getMaxGlobalUsage());
            }
            if (couponRequest.getMinPurchaseAmount() != null) {
                coupon.setMinPurchaseAmount(couponRequest.getMinPurchaseAmount());
            }
            if (couponRequest.getValidFrom() != null) {
                coupon.setValidFrom(couponRequest.getValidFrom());
            }
            if (couponRequest.getValidUntil() != null) {
                coupon.setValidUntil(couponRequest.getValidUntil());
            }
            if (couponRequest.getIsActive() != null) {
                coupon.setIsActive(couponRequest.getIsActive());
            }

            coupon.setUpdatedAt(LocalDateTime.now());
            Coupon updatedCoupon = couponRepo.save(coupon);

            logger.info("Coupon updated successfully: {}", id);
            return new ApiResponse<>(true, "Coupon updated successfully", updatedCoupon, 200);
        } catch (Exception e) {
            logger.error("Error updating coupon: {}", e.getMessage(), e);
            throw new InvalidCourseDataException("Failed to update coupon: " + e.getMessage());
        }
    }

    /**
     * Delete/disable coupon (Admin only)
     */
    public ApiResponse<Void> deleteCoupon(String id) {
        try {
            logger.info("Deleting coupon with ID: {}", id);

            Optional<Coupon> coupon = couponRepo.findById(id);
            if (coupon.isEmpty()) {
                logger.warn("Coupon not found for deletion: {}", id);
                return new ApiResponse<>(false, "Coupon not found", null, 404);
            }

            couponRepo.deleteById(id);
            logger.info("Coupon deleted successfully: {}", id);

            return new ApiResponse<>(true, "Coupon deleted successfully", null, 200);
        } catch (Exception e) {
            logger.error("Error deleting coupon: {}", e.getMessage(), e);
            throw new InvalidCourseDataException("Failed to delete coupon: " + e.getMessage());
        }
    }

    /**
     * Validate coupon for purchase
     * 
     * @param code        Coupon code
     * @param userId      User ID attempting to use coupon
     * @param coursePrice Course price in rupees
     * @return Map with validation result and discount info
     */
    public Map<String, Object> validateCoupon(String code, String userId, Integer coursePrice) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("Validating coupon code: {} for user: {} and price: {}", code, userId, coursePrice);

            // Find coupon by code
            Optional<Coupon> couponOpt = couponRepo.findByCode(code);
            if (couponOpt.isEmpty()) {
                logger.warn("Coupon not found: {}", code);
                result.put("valid", false);
                result.put("message", "Coupon code not found");
                return result;
            }

            Coupon coupon = couponOpt.get();

            // Check if coupon is active
            if (!coupon.getIsActive()) {
                logger.warn("Coupon is inactive: {}", code);
                result.put("valid", false);
                result.put("message", "Coupon has been disabled");
                return result;
            }

            // Check validity dates
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
                logger.warn("Coupon expired or not yet valid: {}", code);
                result.put("valid", false);
                result.put("message", "Coupon has expired or is not yet valid");
                return result;
            }

            // Check global usage limit
            if (coupon.getCurrentGlobalUsage() >= coupon.getMaxGlobalUsage()) {
                logger.warn("Coupon global usage limit exceeded: {}", code);
                result.put("valid", false);
                result.put("message", "Coupon usage limit has been exceeded");
                return result;
            }

            // Check if user has already used this coupon
            Optional<CouponUsage> previousUsage = couponUsageRepo.findByCouponIdAndUserId(coupon.getId(), userId);
            if (previousUsage.isPresent()) {
                logger.warn("User {} has already used coupon: {}", userId, code);
                result.put("valid", false);
                result.put("message", "You have already used this coupon");
                return result;
            }

            // Check minimum purchase amount
            if (coupon.getMinPurchaseAmount() != null && coursePrice < coupon.getMinPurchaseAmount()) {
                logger.warn("Course price {} is less than minimum required: {}", coursePrice,
                        coupon.getMinPurchaseAmount());
                result.put("valid", false);
                result.put("message", "Course price is below minimum required for this coupon");
                return result;
            }

            // Calculate discount
            Integer discountAmount = (coursePrice * coupon.getDiscountPercentage()) / 100;
            Integer finalPrice = coursePrice - discountAmount;

            logger.info("Coupon validated successfully. Discount: {}%, Amount: {}",
                    coupon.getDiscountPercentage(), discountAmount);

            result.put("valid", true);
            result.put("message", "Coupon is valid");
            result.put("couponId", coupon.getId());
            result.put("discountPercentage", coupon.getDiscountPercentage());
            result.put("discountAmount", discountAmount);
            result.put("finalPrice", finalPrice);

            return result;

        } catch (Exception e) {
            logger.error("Error validating coupon: {}", e.getMessage(), e);
            result.put("valid", false);
            result.put("message", "Error validating coupon: " + e.getMessage());
            return result;
        }
    }

    /**
     * Record coupon usage
     * 
     * @param couponId Coupon ID
     * @param userId   User ID
     */
    public void recordCouponUsage(String couponId, String userId) {
        try {
            logger.info("Recording coupon usage - couponId: {}, userId: {}", couponId, userId);

            // Create coupon usage record
            CouponUsage couponUsage = CouponUsage.builder()
                    .couponId(couponId)
                    .userId(userId)
                    .usedAt(LocalDateTime.now())
                    .build();

            couponUsageRepo.save(couponUsage);

            // Increment global usage counter
            Optional<Coupon> couponOpt = couponRepo.findById(couponId);
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                coupon.setCurrentGlobalUsage(coupon.getCurrentGlobalUsage() + 1);
                coupon.setUpdatedAt(LocalDateTime.now());
                couponRepo.save(coupon);
                logger.info("Coupon usage recorded and counter incremented. New count: {}",
                        coupon.getCurrentGlobalUsage());
            }

        } catch (Exception e) {
            logger.error("Error recording coupon usage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to record coupon usage: " + e.getMessage());
        }
    }

    /**
     * Get all active coupons valid for today
     */
    public List<Coupon> getActiveCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepo.findByIsActiveTrueAndValidFromLessThanEqualAndValidUntilGreaterThanEqual(now, now);
    }

    /**
     * Get all coupons used by a user
     */
    public List<CouponUsage> getUserCouponHistory(String userId) {
        logger.info("Fetching coupon history for user: {}", userId);
        return couponUsageRepo.findByUserId(userId);
    }

    /**
     * Get all users who used a specific coupon
     */
    public List<CouponUsage> getCouponUsageHistory(String couponId) {
        logger.info("Fetching usage history for coupon: {}", couponId);
        return couponUsageRepo.findByCouponId(couponId);
    }
}
