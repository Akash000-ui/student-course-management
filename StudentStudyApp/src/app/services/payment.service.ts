import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PaymentInitiationRequest {
    courseId: string;
    couponCode?: string;
}

export interface PaymentInitiationResponse {
    success: boolean;
    message: string;
    data: {
        razorpayOrderId: string;
        razorpayKeyId: string;
        orderId: string;
        courseName: string;
        originalPrice: number;
        discountAmount: number;
        finalAmount: number;
    };
    statusCode: number;
}

export interface PaymentVerificationRequest {
    razorpayOrderId: string;
    razorpayPaymentId: string;
    razorpaySignature: string;
}

export interface PaymentVerificationResponse {
    success: boolean;
    message: string;
    data: {
        orderId: string;
        enrollmentId: string;
        message: string;
    };
    statusCode: number;
}

export interface Order {
    id: string;
    userId: string;
    courseId: string;
    originalPrice: number;
    discountAmount: number;
    couponCode?: string;
    finalAmount: number;
    razorpayOrderId: string;
    razorpayPaymentId: string;
    paymentStatus: string;
    orderStatus: string;
    enrollmentId: string;
    createdAt: string;
    completedAt: string;
}

@Injectable({
    providedIn: 'root'
})
export class PaymentService {

    private apiUrl = '/api/payments';

    constructor(private http: HttpClient) { }

    /**
     * Initiate payment for a course
     */
    initiatePayment(request: PaymentInitiationRequest): Observable<PaymentInitiationResponse> {
        return this.http.post<PaymentInitiationResponse>(
            `${this.apiUrl}/initiate`,
            request,
            {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json'
                })
            }
        );
    }

    /**
     * Verify payment after Razorpay checkout
     */
    verifyPayment(request: PaymentVerificationRequest): Observable<PaymentVerificationResponse> {
        return this.http.post<PaymentVerificationResponse>(
            `${this.apiUrl}/verify`,
            request,
            {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json'
                })
            }
        );
    }

    /**
     * Get all orders for current user
     */
    getUserOrders(): Observable<any> {
        return this.http.get<any>(
            `${this.apiUrl}/orders`,
            {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json'
                })
            }
        );
    }

    /**
     * Get specific order details
     */
    getOrderDetails(orderId: string): Observable<any> {
        return this.http.get<any>(
            `${this.apiUrl}/orders/${orderId}`,
            {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json'
                })
            }
        );
    }

    /**
     * Get payment statistics (Admin only)
     */
    getPaymentStats(): Observable<any> {
        return this.http.get<any>(
            `${this.apiUrl}/stats`,
            {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json'
                })
            }
        );
    }
}
