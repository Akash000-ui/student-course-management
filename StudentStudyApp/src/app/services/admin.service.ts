import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CourseStats {
  courseId: string;
  courseTitle: string;
  totalEnrollments: number;
  activeEnrollments: number;
  completedEnrollments: number;
  totalVideos: number;
}

export interface RecentActivity {
  newUsersToday: number;
  newEnrollmentsToday: number;
  activeUsersToday: number;
  mostPopularCourse: string;
  mostPopularCourseEnrollments: number;
}

export interface AdminDashboardStats {
  totalUsers: number;
  totalCourses: number;
  totalEnrollments: number;
  totalVideos: number;
  newUsersThisMonth: number;
  newEnrollmentsThisMonth: number;
  courseStats: CourseStats[];
  recentActivity: RecentActivity;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  statusCode: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  // private baseUrl = 'https://studiehub-backend-latest.onrender.com/api/admin';
  private baseUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) { }

  getAdminStats(): Observable<ApiResponse<AdminDashboardStats>> {
    return this.http.get<ApiResponse<AdminDashboardStats>>(`${this.baseUrl}/stats`);
  }

  // ==================== PAYMENT ANALYTICS ====================

  /**
   * Get dashboard summary
   */
  getDashboardSummary(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/dashboard/summary`);
  }

  /**
   * Get revenue statistics
   */
  getRevenueStats(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/analytics/revenue`);
  }

  /**
   * Get top selling courses
   */
  getTopCourses(limit: number = 10): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/analytics/top-courses`, {
      params: { limit: limit.toString() }
    });
  }

  /**
   * Get payment status distribution
   */
  getPaymentStatusDistribution(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/analytics/payment-status`);
  }

  /**
   * Get coupon statistics
   */
  getCouponStats(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/analytics/coupons`);
  }

  /**
   * Get most used coupons
   */
  getMostUsedCoupons(limit: number = 10): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/coupons/stats/most-used`, {
      params: { limit: limit.toString() }
    });
  }

  /**
   * Get payment failures
   */
  getPaymentFailures(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/analytics/failures`);
  }

  /**
   * Get daily revenue chart data
   */
  getDailyRevenueChart(days: number = 30): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/analytics/daily-revenue`, {
      params: { days: days.toString() }
    });
  }

  // ==================== COUPON MANAGEMENT ====================

  /**
   * Get all coupons
   */
  getAllCoupons(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/coupons`);
  }

  /**
   * Create coupon
   */
  createCoupon(coupon: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/coupons`, coupon);
  }

  /**
   * Update coupon
   */
  updateCoupon(id: string, coupon: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/coupons/${id}`, coupon);
  }

  /**
   * Delete coupon
   */
  deleteCoupon(id: string): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/coupons/${id}`);
  }

  // ==================== ORDER MANAGEMENT ====================

  /**
   * Get all orders
   */
  getAllOrders(paymentStatus?: string): Observable<any> {
    let params: any = {};
    if (paymentStatus) {
      params.paymentStatus = paymentStatus;
    }
    return this.http.get<any>(`${this.baseUrl}/orders`, { params });
  }

  /**
   * Get order details
   */
  getOrderDetails(orderId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/orders/${orderId}`);
  }

  /**
   * Get user orders
   */
  getUserOrders(userId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/users/${userId}/orders`);
  }

  /**
   * Get course orders
   */
  getCourseOrders(courseId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/courses/${courseId}/orders`);
  }

  /**
   * Process refund
   */
  refundOrder(orderId: string, reason: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/orders/${orderId}/refund`, { reason });
  }
}
