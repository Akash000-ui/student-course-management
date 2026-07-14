import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseService } from '../../../services/course.service';
import { PaymentService, PaymentInitiationResponse } from '../../../services/payment.service';
import { CouponService, CouponValidationResponse } from '../../../services/coupon.service';
import { AuthService } from '../../../services/auth.service';

@Component({
    selector: 'app-checkout',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './checkout.component.html',
    styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {

    courseId: string = '';
    course: any = null;
    coursePrice: number = 0;

    // Coupon fields
    couponCode: string = '';
    couponDiscount: number = 0;
    finalPrice: number = 0;
    couponApplied: boolean = false;
    couponError: string = '';
    couponLoading: boolean = false;

    // Payment fields
    paymentInitiated: boolean = false;
    paymentLoading: boolean = false;
    razorpayOrderId: string = '';
    razorpayKeyId: string = '';
    orderId: string = '';

    // UI state
    isLoading: boolean = true;
    error: string = '';
    successMessage: string = '';
    currentUserId: string = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private courseService: CourseService,
        private paymentService: PaymentService,
        private couponService: CouponService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        // Try to get courseId from both route params and query params
        this.route.params.subscribe(params => {
            this.courseId = params['courseId'] || '';
        });

        // Also check query params (when navigating from course detail page)
        this.route.queryParams.subscribe(queryParams => {
            if (queryParams['courseId']) {
                this.courseId = queryParams['courseId'];
            }
        });

        this.currentUserId = this.authService.getCurrentUser()?.id || '';
        if (this.courseId) {
            this.loadCourseDetails();
            this.loadRazorpayScript();
        } else {
            this.error = 'No course selected for purchase';
            this.isLoading = false;
        }
    }

    /**
     * Load course details
     */
    loadCourseDetails(): void {
        this.courseService.getCourseById(this.courseId).subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.course = response.data;
                    this.coursePrice = this.course.price || 0;
                    this.finalPrice = this.coursePrice; // Initially same as original price
                    this.isLoading = false;
                } else {
                    this.error = 'Failed to load course details';
                    this.isLoading = false;
                }
            },
            error: (err: any) => {
                this.error = 'Error loading course: ' + err.message;
                this.isLoading = false;
            }
        });
    }

    /**
     * Load Razorpay SDK script
     */
    private loadRazorpayScript(): void {
        const script = document.createElement('script');
        script.src = 'https://checkout.razorpay.com/v1/checkout.js';
        script.async = true;
        script.onload = () => {
            console.log('Razorpay SDK loaded successfully');
        };
        script.onerror = () => {
            console.error('Failed to load Razorpay SDK');
            this.error = 'Failed to load payment gateway. Please try again.';
        };
        document.body.appendChild(script);
    }

    /**
     * Validate and apply coupon
     */
    applyCoupon(): void {
        if (!this.couponCode.trim()) {
            this.couponError = 'Please enter a coupon code';
            return;
        }

        this.couponLoading = true;
        this.couponError = '';

        this.couponService.validateCoupon(this.couponCode, this.currentUserId, this.coursePrice).subscribe({
            next: (response: CouponValidationResponse) => {
                this.couponLoading = false;

                if (response.data.valid) {
                    this.couponApplied = true;
                    this.couponDiscount = response.data.discountAmount || 0;
                    this.finalPrice = response.data.finalPrice || this.coursePrice;
                    this.couponError = '';
                } else {
                    this.couponApplied = false;
                    this.couponError = response.data.message || 'Invalid coupon code';
                    this.couponDiscount = 0;
                    this.finalPrice = this.coursePrice;
                }
            },
            error: (err: any) => {
                this.couponLoading = false;
                this.couponError = 'Error validating coupon: ' + (err.error?.message || err.message);
                this.couponApplied = false;
                this.couponDiscount = 0;
                this.finalPrice = this.coursePrice;
            }
        });
    }

    /**
     * Remove coupon
     */
    removeCoupon(): void {
        this.couponCode = '';
        this.couponApplied = false;
        this.couponDiscount = 0;
        this.finalPrice = this.coursePrice;
        this.couponError = '';
    }

    /**
     * Initiate payment
     */
    initiatePayment(): void {
        this.paymentLoading = true;
        this.error = '';

        const paymentRequest = {
            courseId: this.courseId,
            couponCode: this.couponApplied ? this.couponCode : undefined
        };

        this.paymentService.initiatePayment(paymentRequest).subscribe({
            next: (response: PaymentInitiationResponse) => {
                this.paymentLoading = false;

                if (response.success) {
                    this.razorpayOrderId = response.data.razorpayOrderId;
                    this.razorpayKeyId = response.data.razorpayKeyId;
                    this.orderId = response.data.orderId;
                    this.paymentInitiated = true;

                    // Trigger Razorpay checkout
                    setTimeout(() => {
                        this.openRazorpayCheckout();
                    }, 500);
                } else {
                    this.error = response.message;
                    this.paymentInitiated = false;
                }
            },
            error: (err: any) => {
                this.paymentLoading = false;
                this.error = 'Payment initiation failed: ' + (err.error?.message || err.message);
                this.paymentInitiated = false;
            }
        });
    }

    /**
     * Open Razorpay checkout modal
     */
    private openRazorpayCheckout(): void {
        const options = {
            key: this.razorpayKeyId,
            amount: this.finalPrice * 100, // Amount in paise
            currency: 'INR',
            name: 'Study App',
            description: this.course.title,
            order_id: this.razorpayOrderId,
            handler: (response: any) => {
                this.handlePaymentSuccess(response);
            },
            prefill: {
                name: localStorage.getItem('userName') || '',
                email: localStorage.getItem('userEmail') || '',
                contact: localStorage.getItem('userPhone') || ''
            },
            notes: {
                courseId: this.courseId,
                courseName: this.course.title
            },
            theme: {
                color: '#3399cc'
            },
            modal: {
                ondismiss: () => {
                    this.handlePaymentCancel();
                }
            }
        };

        // Wait for Razorpay SDK to load
        const checkRazorpay = () => {
            if ((window as any).Razorpay) {
                const rzp = new (window as any).Razorpay(options);
                rzp.open();
                console.log('Razorpay checkout opened successfully');
            } else {
                // Retry after a short delay
                setTimeout(checkRazorpay, 500);
            }
        };

        checkRazorpay();
    }

    /**
     * Handle successful payment
     */
    private handlePaymentSuccess(response: any): void {
        const verificationRequest = {
            razorpayOrderId: this.razorpayOrderId,
            razorpayPaymentId: response.razorpay_payment_id,
            razorpaySignature: response.razorpay_signature
        };

        this.paymentService.verifyPayment(verificationRequest).subscribe({
            next: (verifyResponse: any) => {
                if (verifyResponse.success) {
                    this.successMessage = 'Payment successful! Course access has been granted.';
                    localStorage.setItem('lastEnrollmentId', verifyResponse.data.enrollmentId);

                    // Redirect to course after 2 seconds
                    setTimeout(() => {
                        this.router.navigate(['/course', this.courseId]);
                    }, 2000);
                } else {
                    this.error = verifyResponse.message;
                }
            },
            error: (err: any) => {
                this.error = 'Payment verification failed: ' + (err.error?.message || err.message);
            }
        });
    }

    /**
     * Handle payment cancellation
     */
    private handlePaymentCancel(): void {
        this.error = 'Payment cancelled. Please try again.';
        this.paymentInitiated = false;
    }

    /**
     * Cancel and go back
     */
    cancelPayment(): void {
        this.router.navigate(['/course', this.courseId]);
    }
}
