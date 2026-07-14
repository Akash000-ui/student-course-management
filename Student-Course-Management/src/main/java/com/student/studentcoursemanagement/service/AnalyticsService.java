package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.model.Coupon;
import com.student.studentcoursemanagement.model.CouponUsage;
import com.student.studentcoursemanagement.model.Order;
import com.student.studentcoursemanagement.model.PaymentStatus;
import com.student.studentcoursemanagement.repo.CouponRepo;
import com.student.studentcoursemanagement.repo.CouponUsageRepo;
import com.student.studentcoursemanagement.repo.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics Service for payment statistics and reporting
 * Provides insights into revenue, coupon usage, and payment trends
 */
@Service
public class AnalyticsService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CouponRepo couponRepo;

    @Autowired
    private CouponUsageRepo couponUsageRepo;

    /**
     * Get total revenue statistics
     */
    public Map<String, Object> getTotalRevenueStats() {
        List<Order> completedOrders = orderRepo.findByPaymentStatus(PaymentStatus.COMPLETED);

        double totalRevenue = completedOrders.stream()
                .mapToDouble(Order::getFinalAmount)
                .sum();

        double totalDiscount = completedOrders.stream()
                .mapToDouble(Order::getDiscountAmount)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalOrders", completedOrders.size());
        stats.put("totalDiscountGiven", totalDiscount);
        stats.put("averageOrderValue", completedOrders.isEmpty() ? 0 : totalRevenue / completedOrders.size());

        return stats;
    }

    /**
     * Get revenue by date range
     */
    public Map<String, Object> getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepo.findAll().stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.COMPLETED)
                .filter(o -> {
                    LocalDateTime createdAt = LocalDateTime.parse(o.getCreatedAt().toString());
                    return createdAt.isAfter(startDate) && createdAt.isBefore(endDate);
                })
                .collect(Collectors.toList());

        double revenue = orders.stream().mapToDouble(Order::getFinalAmount).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("revenue", revenue);
        stats.put("orderCount", orders.size());
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);

        return stats;
    }

    /**
     * Get top selling courses
     */
    public List<Map<String, Object>> getTopSellingCourses(int limit) {
        List<Order> completedOrders = orderRepo.findByPaymentStatus(PaymentStatus.COMPLETED);

        return completedOrders.stream()
                .collect(Collectors.groupingBy(
                        Order::getCourseId,
                        Collectors.summingDouble(Order::getFinalAmount)))
                .entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> course = new HashMap<>();
                    course.put("courseId", entry.getKey());
                    course.put("totalRevenue", entry.getValue());
                    course.put("orderCount", completedOrders.stream()
                            .filter(o -> o.getCourseId().equals(entry.getKey()))
                            .count());
                    return course;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get payment status distribution
     */
    public Map<String, Long> getPaymentStatusDistribution() {
        List<Order> allOrders = orderRepo.findAll();

        return allOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getPaymentStatus().name(),
                        Collectors.counting()));
    }

    /**
     * Get coupon usage statistics
     */
    public Map<String, Object> getCouponUsageStats() {
        List<Coupon> coupons = couponRepo.findAll();
        List<CouponUsage> usages = couponUsageRepo.findAll();

        double totalDiscountFromCoupons = 0;
        int totalCouponUsages = usages.size();

        // Count how many users each coupon reached
        Map<String, Long> couponUserCount = usages.stream()
                .collect(Collectors.groupingBy(CouponUsage::getCouponId, Collectors.counting()));

        long totalUniqueUsersCouponUsed = couponUserCount.size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("activeCoupons", coupons.stream().filter(Coupon::getIsActive).count());
        stats.put("totalCoupons", coupons.size());
        stats.put("totalCouponUsages", totalCouponUsages);
        stats.put("uniqueUsersWithCoupons", totalUniqueUsersCouponUsed);
        stats.put("couponUtilizationRate", coupons.isEmpty() ? 0 : (double) totalCouponUsages / coupons.size());

        return stats;
    }

    /**
     * Get most used coupons
     */
    public List<Map<String, Object>> getMostUsedCoupons(int limit) {
        List<CouponUsage> usages = couponUsageRepo.findAll();

        return usages.stream()
                .collect(Collectors.groupingBy(
                        CouponUsage::getCouponId,
                        Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(entry -> {
                    Coupon coupon = couponRepo.findById(entry.getKey()).orElse(null);
                    Map<String, Object> couponStats = new HashMap<>();
                    couponStats.put("couponId", entry.getKey());
                    couponStats.put("couponCode", coupon != null ? coupon.getCode() : "Unknown");
                    couponStats.put("usageCount", entry.getValue());
                    couponStats.put("discountPercentage", coupon != null ? coupon.getDiscountPercentage() : 0);
                    return couponStats;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get payment failures and reasons
     */
    public Map<String, Object> getPaymentFailures() {
        List<Order> failedOrders = orderRepo.findByPaymentStatus(PaymentStatus.FAILED);

        Map<String, Object> stats = new HashMap<>();
        stats.put("failedOrderCount", failedOrders.size());
        stats.put("failureRate",
                orderRepo.findAll().isEmpty() ? 0 : (double) failedOrders.size() / orderRepo.findAll().size() * 100);
        stats.put("totalLostRevenue", failedOrders.stream()
                .mapToDouble(Order::getFinalAmount)
                .sum());

        return stats;
    }

    /**
     * Get daily revenue for chart
     */
    public List<Map<String, Object>> getDailyRevenueChart(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minus(days, ChronoUnit.DAYS);

        List<Order> orders = orderRepo.findAll().stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<LocalDate, Double> dailyRevenue = new LinkedHashMap<>();

        // Initialize all dates with 0
        for (int i = 0; i < days; i++) {
            dailyRevenue.put(startDate.toLocalDate().plusDays(i), 0.0);
        }

        // Aggregate revenue by date
        orders.forEach(order -> {
            try {
                LocalDate orderDate = LocalDateTime.parse(order.getCreatedAt().toString()).toLocalDate();
                if (orderDate.isAfter(startDate.toLocalDate()) && orderDate.isBefore(endDate.toLocalDate())) {
                    dailyRevenue.put(orderDate, dailyRevenue.getOrDefault(orderDate, 0.0) + order.getFinalAmount());
                }
            } catch (Exception e) {
                // Skip invalid dates
            }
        });

        return dailyRevenue.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> daily = new HashMap<>();
                    daily.put("date", entry.getKey());
                    daily.put("revenue", entry.getValue());
                    return daily;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get overall dashboard summary
     */
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Revenue stats
        Map<String, Object> revenueStats = getTotalRevenueStats();
        summary.put("totalRevenue", revenueStats.get("totalRevenue"));
        summary.put("totalOrders", revenueStats.get("totalOrders"));
        summary.put("averageOrderValue", revenueStats.get("averageOrderValue"));

        // Payment status distribution
        summary.put("paymentStatusDistribution", getPaymentStatusDistribution());

        // Coupon stats
        Map<String, Object> couponStats = getCouponUsageStats();
        summary.put("activeCoupons", couponStats.get("activeCoupons"));
        summary.put("couponUsageRate", couponStats.get("couponUtilizationRate"));

        // Failures
        Map<String, Object> failureStats = getPaymentFailures();
        summary.put("failureRate", failureStats.get("failureRate"));

        // Top courses
        summary.put("topCourses", getTopSellingCourses(5));

        // Daily chart data
        summary.put("dailyRevenue", getDailyRevenueChart(30));

        return summary;
    }
}
