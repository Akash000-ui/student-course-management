import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OrderHistoryComponent } from './order-history.component';
import { PaymentService } from '../services/payment.service';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';

describe('OrderHistoryComponent', () => {
    let component: OrderHistoryComponent;
    let fixture: ComponentFixture<OrderHistoryComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [OrderHistoryComponent],
            providers: [
                {
                    provide: PaymentService,
                    useValue: {
                        getUserOrders: jasmine.createSpy('getUserOrders').and.returnValue(
                            of({
                                success: true,
                                data: []
                            })
                        )
                    }
                },
                {
                    provide: AuthService,
                    useValue: {}
                },
                {
                    provide: Router,
                    useValue: {
                        navigate: jasmine.createSpy('navigate')
                    }
                }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(OrderHistoryComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load order history on init', () => {
        expect(component.orders).toEqual([]);
    });

    it('should show empty state when no orders', () => {
        expect(component.orders.length).toBe(0);
    });
});
