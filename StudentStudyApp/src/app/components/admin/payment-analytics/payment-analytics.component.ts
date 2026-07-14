import { Component, OnInit } from '@angular/core';
import { AdminService } from '../services/admin.service';

@Component({
    selector: 'app-payment-analytics',
    templateUrl: './payment-analytics.component.html',
    styleUrls: ['./payment-analytics.component.css']
})
export class PaymentAnalyticsComponent implements OnInit {

    // Revenue data
    revenueStats: any = null;
    totalRevenue: number = 0;
    totalOrders: number = 0;
    averageOrderValue: number = 0;
    totalDiscount: number = 0;

    // Top courses
    topCourses: any[] = [];

    // Payment status
    paymentStatusDistribution: any = null;
    paymentStatusChart: any = null;

    // Coupon stats
    couponStats: any = null;
    mostUsedCoupons: any[] = [];

    // Failure stats
    failureStats: any = null;
    failureRate: number = 0;

    // Daily revenue
    dailyRevenueData: any[] = [];

    // Loading state
    isLoading: boolean = true;
    error: string = '';

    // Chart configuration
    chartDays: number = 30;
    selectedPeriod: string = '30days';

    constructor(private adminService: AdminService) { }

    ngOnInit(): void {
        this.loadAnalytics();
    }

    /**
     * Load all analytics data
     */
    loadAnalytics(): void {
        this.isLoading = true;
        this.error = '';

        // Load revenue stats
        this.adminService.getRevenueStats().subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.revenueStats = response.data;
                    this.totalRevenue = response.data.totalRevenue || 0;
                    this.totalOrders = response.data.totalOrders || 0;
                    this.averageOrderValue = response.data.averageOrderValue || 0;
                    this.totalDiscount = response.data.totalDiscountGiven || 0;
                }
            },
            error: (err) => {
                console.error('Error loading revenue stats', err);
            }
        });

        // Load top courses
        this.adminService.getTopCourses(10).subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.topCourses = response.data || [];
                }
            },
            error: (err) => {
                console.error('Error loading top courses', err);
            }
        });

        // Load payment status distribution
        this.adminService.getPaymentStatusDistribution().subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.paymentStatusDistribution = response.data;
                    this.preparePaymentStatusChart();
                }
            },
            error: (err) => {
                console.error('Error loading payment status', err);
            }
        });

        // Load coupon stats
        this.adminService.getCouponStats().subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.couponStats = response.data;
                }
            },
            error: (err) => {
                console.error('Error loading coupon stats', err);
            }
        });

        // Load most used coupons
        this.adminService.getMostUsedCoupons(5).subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.mostUsedCoupons = response.data || [];
                }
            },
            error: (err) => {
                console.error('Error loading most used coupons', err);
            }
        });

        // Load failure stats
        this.adminService.getPaymentFailures().subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.failureStats = response.data;
                    this.failureRate = response.data.failureRate || 0;
                }
                this.isLoading = false;
            },
            error: (err) => {
                console.error('Error loading failure stats', err);
                this.isLoading = false;
            }
        });

        // Load daily revenue chart
        this.loadDailyRevenue();
    }

    /**
     * Load daily revenue
     */
    loadDailyRevenue(): void {
        this.adminService.getDailyRevenueChart(this.chartDays).subscribe({
            next: (response: any) => {
                if (response.success) {
                    this.dailyRevenueData = response.data || [];
                }
            },
            error: (err) => {
                console.error('Error loading daily revenue', err);
            }
        });
    }

    /**
     * Change chart period
     */
    changePeriod(period: string): void {
        this.selectedPeriod = period;
        switch (period) {
            case '7days':
                this.chartDays = 7;
                break;
            case '30days':
                this.chartDays = 30;
                break;
            case '90days':
                this.chartDays = 90;
                break;
            default:
                this.chartDays = 30;
        }
        this.loadDailyRevenue();
    }

    /**
     * Prepare payment status chart data
     */
    preparePaymentStatusChart(): void {
        if (!this.paymentStatusDistribution) return;

        this.paymentStatusChart = {
            labels: Object.keys(this.paymentStatusDistribution),
            data: Object.values(this.paymentStatusDistribution),
            colors: this.getStatusColors()
        };
    }

    /**
     * Get status colors for chart
     */
    getStatusColors(): string[] {
        const colors: { [key: string]: string } = {
            'COMPLETED': '#28a745',
            'PENDING': '#ffc107',
            'FAILED': '#dc3545'
        };

        return Object.keys(this.paymentStatusDistribution).map(status =>
            colors[status] || '#6c757d'
        );
    }

    /**
     * Get performance rating
     */
    getPerformanceRating(): string {
        if (this.failureRate < 1) return 'Excellent';
        if (this.failureRate < 5) return 'Good';
        if (this.failureRate < 10) return 'Fair';
        return 'Poor';
    }

    /**
     * Format currency
     */
    formatCurrency(value: number): string {
        return '₹' + (value || 0).toFixed(2);
    }

    /**
     * Format percentage
     */
    formatPercentage(value: number): string {
        return (value || 0).toFixed(2) + '%';
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

    /**
     * Refresh analytics
     */
    refresh(): void {
        this.loadAnalytics();
    }

    /**
     * Export report (placeholder)
     */
    exportReport(): void {
        alert('Export functionality coming soon!');
    }

    /**
     * Get revenue trend
     */
    getRevenueTrend(): string {
        if (this.dailyRevenueData.length < 2) return 'N/A';

        const firstDay = this.dailyRevenueData[0]?.revenue || 0;
        const lastDay = this.dailyRevenueData[this.dailyRevenueData.length - 1]?.revenue || 0;

        if (lastDay > firstDay) return '↑ Up';
        if (lastDay < firstDay) return '↓ Down';
        return '→ Stable';
    }
}
