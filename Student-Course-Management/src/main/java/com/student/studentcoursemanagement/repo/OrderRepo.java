package com.student.studentcoursemanagement.repo;

import com.student.studentcoursemanagement.model.Order;
import com.student.studentcoursemanagement.model.OrderStatus;
import com.student.studentcoursemanagement.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface OrderRepo extends MongoRepository<Order, String> {

    // Find order by Razorpay order ID
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    // Find order by Razorpay payment ID
    Optional<Order> findByRazorpayPaymentId(String razorpayPaymentId);

    // Get all orders for a user
    List<Order> findByUserId(String userId);

    // Get all orders for a specific course
    List<Order> findByCourseId(String courseId);

    // Get user's order for a specific course
    Optional<Order> findByUserIdAndCourseId(String userId, String courseId);

    // Find completed orders by user
    List<Order> findByUserIdAndOrderStatus(String userId, OrderStatus orderStatus);

    // Find paid orders
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);

    // Find orders by enrollment ID
    Optional<Order> findByEnrollmentId(String enrollmentId);
}
