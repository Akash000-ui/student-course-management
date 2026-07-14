import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PaymentService, Order } from '../services/payment.service';
import { AuthService } from '../services/auth.service';

@Component({
    selector: 'app-order-history',
    templateUrl: './order-history.component.html',
    styleUrls: ['./order-history.component.css']
})
export class OrderHistoryComponent implements OnInit {

    orders: Order[] = [];
    isLoading: boolean = true;
    error: string = '';
    selectedOrder: Order | null = null;
    showReceiptModal: boolean = false;

    constructor(
        private paymentService: PaymentService,
        private authService: AuthService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.loadOrderHistory();
    }

    /**
     * Load user's order history
     */
    loadOrderHistory(): void {
        this.isLoading = true;
        this.error = '';

        this.paymentService.getUserOrders().subscribe({
            next: (response: any) => {
                this.isLoading = false;
                if (response.success) {
                    this.orders = response.data || [];
                } else {
                    this.error = response.message;
                }
            },
            error: (err) => {
                this.isLoading = false;
                this.error = 'Failed to load order history: ' + (err.error?.message || err.message);
            }
        });
    }

    /**
     * View receipt
     */
    viewReceipt(order: Order): void {
        this.selectedOrder = order;
        this.showReceiptModal = true;
    }

    /**
     * Close receipt modal
     */
    closeReceiptModal(): void {
        this.showReceiptModal = false;
        this.selectedOrder = null;
    }

    /**
     * Download receipt as PDF (placeholder)
     */
    downloadReceipt(order: Order): void {
        alert('Receipt download feature coming soon!');
    }

    /**
     * Get payment status badge class
     */
    getPaymentStatusClass(status: string): string {
        return 'status-' + status.toLowerCase();
    }

    /**
     * Get order status badge class
     */
    getOrderStatusClass(status: string): string {
        return 'order-status-' + status.toLowerCase();
    }

    /**
     * Format date
     */
    formatDate(dateString: string): string {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    /**
     * Go back
     */
    goBack(): void {
        this.router.navigate(['/dashboard']);
    }
}
