import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CouponValidationRequest {
    code: string;
    userId: string;
    coursePrice: number;
}

export interface CouponValidationResponse {
    success: boolean;
    message: string;
    data: {
        valid: boolean;
        discountPercentage?: number;
        discountAmount?: number;
        finalPrice?: number;
        message?: string;
    };
    statusCode: number;
}

export interface Coupon {
    id: string;
    code: string;
    discountPercentage: number;
    maxGlobalUsage: number;
    currentGlobalUsage: number;
    minPurchaseAmount: number;
    validFrom: string;
    validUntil: string;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
    createdBy: string;
}

@Injectable({
    providedIn: 'root'
})
export class CouponService {

    private apiUrl = '/api/coupons';

    constructor(private http: HttpClient) { }

    /**
     * Validate coupon for a course
     */
    validateCoupon(code: string, userId: string, coursePrice: number): Observable<CouponValidationResponse> {
        return this.http.get<CouponValidationResponse>(
            `${this.apiUrl}/validate/${code}`,
            {
                params: {
                    userId: userId,
                    coursePrice: coursePrice.toString()
                },
                headers: new HttpHeaders({
                    'Content-Type': 'application/json'
                })
            }
        );
    }

    /**
     * Get all active coupons
     */
    getActiveCoupons(): Observable<any> {
        return this.http.get<any>(
            `${this.apiUrl}/active`,
            {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json'
                })
            }
        );
    }

    /**
     * Get user's coupon history
     */
    getUserCouponHistory(userId: string): Observable<any> {
        return this.http.get<any>(
            `${this.apiUrl}/user/${userId}/history`,
            {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json'
                })
            }
        );
    }
}
