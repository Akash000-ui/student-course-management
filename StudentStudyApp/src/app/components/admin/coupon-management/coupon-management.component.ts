import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../services/admin.service';
import { CouponService } from '../../../services/coupon.service';
@Component({
    selector: 'app-coupon-management',
    templateUrl: './coupon-management.component.html',
    styleUrls: ['./coupon-management.component.css'],
    standalone: false
})
export class CouponManagementComponent implements OnInit {

    coupons: any[] = [];
    isLoading: boolean = true;
    error: string = '';

    // Form state
    showCreateForm: boolean = false;
    showEditForm: boolean = false;
    selectedCoupon: any = null;

    // Form data
    formData = {
        code: '',
        discountPercentage: 10,
        maxGlobalUsage: 100,
        minPurchaseAmount: 0,
        validFrom: '',
        validUntil: '',
        isActive: true
    };

    constructor(private adminService: AdminService) { }

    ngOnInit(): void {
        this.loadCoupons();
    }

    /**
     * Load all coupons
     */
    loadCoupons(): void {
        this.isLoading = true;
        this.error = '';

        this.adminService.getAllCoupons().subscribe({
            next: (response: any) => {
                this.isLoading = false;
                if (response.success) {
                    this.coupons = response.data || [];
                } else {
                    this.error = response.message;
                }
            },
            error: (err: any) => {
                this.isLoading = false;
                this.error = 'Failed to load coupons: ' + (err.error?.message || err.message);
            }
        });
    }

    /**
     * Show create coupon form
     */
    showCreate(): void {
        this.resetForm();
        this.showCreateForm = true;
        this.showEditForm = false;
    }

    /**
     * Hide forms
     */
    hideForm(): void {
        this.showCreateForm = false;
        this.showEditForm = false;
        this.selectedCoupon = null;
        this.resetForm();
    }

    /**
     * Reset form
     */
    resetForm(): void {
        this.formData = {
            code: '',
            discountPercentage: 10,
            maxGlobalUsage: 100,
            minPurchaseAmount: 0,
            validFrom: '',
            validUntil: '',
            isActive: true
        };
    }

    /**
     * Create coupon
     */
    createCoupon(): void {
        if (!this.validateForm()) {
            return;
        }

        this.adminService.createCoupon(this.formData).subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.loadCoupons();
                    this.hideForm();
                    alert('Coupon created successfully!');
                } else {
                    alert('Failed to create coupon: ' + response.message);
                }
            },
            error: (err: any) => {
                alert('Error creating coupon: ' + (err.error?.message || err.message));
            }
        });
    }

    /**
     * Edit coupon - show form
     */
    editCoupon(coupon: any): void {
        this.selectedCoupon = coupon;
        this.formData = { ...coupon };
        this.showEditForm = true;
        this.showCreateForm = false;
    }

    /**
     * Update coupon
     */
    updateCoupon(): void {
        if (!this.validateForm()) {
            return;
        }

        this.adminService.updateCoupon(this.selectedCoupon.id, this.formData).subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.loadCoupons();
                    this.hideForm();
                    alert('Coupon updated successfully!');
                } else {
                    alert('Failed to update coupon: ' + response.message);
                }
            },
            error: (err: any) => {
                alert('Error updating coupon: ' + (err.error?.message || err.message));
            }
        });
    }

    /**
     * Delete coupon
     */
    deleteCoupon(coupon: any): void {
        if (!confirm(`Are you sure you want to delete coupon "${coupon.code}"?`)) {
            return;
        }

        this.adminService.deleteCoupon(coupon.id).subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.loadCoupons();
                    alert('Coupon deleted successfully!');
                } else {
                    alert('Failed to delete coupon: ' + response.message);
                }
            },
            error: (err: any) => {
                alert('Error deleting coupon: ' + (err.error?.message || err.message));
            }
        });
    }

    /**
     * Validate form
     */
    validateForm(): boolean {
        if (!this.formData.code.trim()) {
            alert('Please enter coupon code');
            return false;
        }
        if (this.formData.discountPercentage < 1 || this.formData.discountPercentage > 100) {
            alert('Discount percentage must be between 1 and 100');
            return false;
        }
        if (this.formData.maxGlobalUsage < 1) {
            alert('Max global usage must be at least 1');
            return false;
        }
        if (!this.formData.validFrom) {
            alert('Please enter valid from date');
            return false;
        }
        if (!this.formData.validUntil) {
            alert('Please enter valid until date');
            return false;
        }
        if (new Date(this.formData.validFrom) >= new Date(this.formData.validUntil)) {
            alert('Valid until date must be after valid from date');
            return false;
        }
        return true;
    }

    /**
     * Get remaining uses
     */
    getRemainingUses(coupon: any): number {
        return (coupon.maxGlobalUsage || 0) - (coupon.currentGlobalUsage || 0);
    }

    /**
     * Get usage percentage
     */
    getUsagePercentage(coupon: any): number {
        if (!coupon.maxGlobalUsage) return 0;
        return (coupon.currentGlobalUsage / coupon.maxGlobalUsage) * 100;
    }

    /**
     * Is coupon expired
     */
    isCouponExpired(coupon: any): boolean {
        return new Date(coupon.validUntil) < new Date();
    }

    /**
     * Get status badge class
     */
    getStatusBadgeClass(coupon: any): string {
        if (this.isCouponExpired(coupon)) {
            return 'badge-danger';
        }
        if (!coupon.isActive) {
            return 'badge-secondary';
        }
        return 'badge-success';
    }

    /**
     * Get status text
     */
    getStatusText(coupon: any): string {
        if (this.isCouponExpired(coupon)) {
            return 'Expired';
        }
        if (!coupon.isActive) {
            return 'Inactive';
        }
        return 'Active';
    }

    /**
     * Format date
     */
    formatDate(dateString: string): string {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }
}
