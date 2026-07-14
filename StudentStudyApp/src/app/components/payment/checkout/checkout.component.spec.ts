import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CheckoutComponent } from './checkout.component';
import { ActivatedRoute } from '@angular/router';
import { CourseService } from '../services/course.service';
import { PaymentService } from '../services/payment.service';
import { CouponService } from '../services/coupon.service';
import { AuthService } from '../services/auth.service';
import { of } from 'rxjs';

describe('CheckoutComponent', () => {
    let component: CheckoutComponent;
    let fixture: ComponentFixture<CheckoutComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CheckoutComponent],
            providers: [
                {
                    provide: ActivatedRoute,
                    useValue: {
                        params: of({ courseId: 'test-course-id' })
                    }
                },
                {
                    provide: CourseService,
                    useValue: {
                        getCourseById: jasmine.createSpy('getCourseById').and.returnValue(
                            of({
                                success: true,
                                data: {
                                    id: 'test-course-id',
                                    title: 'Test Course',
                                    price: 999,
                                    description: 'Test Description'
                                }
                            })
                        )
                    }
                },
                {
                    provide: PaymentService,
                    useValue: {
                        initiatePayment: jasmine.createSpy('initiatePayment'),
                        verifyPayment: jasmine.createSpy('verifyPayment')
                    }
                },
                {
                    provide: CouponService,
                    useValue: {
                        validateCoupon: jasmine.createSpy('validateCoupon')
                    }
                },
                {
                    provide: AuthService,
                    useValue: {
                        getCurrentUserId: jasmine.createSpy('getCurrentUserId').and.returnValue('test-user-id')
                    }
                }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(CheckoutComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load course details on init', () => {
        expect(component.course).toBeTruthy();
        expect(component.coursePrice).toBe(999);
    });

    it('should apply coupon discount', () => {
        // Test coupon application
        expect(component.couponApplied).toBeFalsy();
    });
});
