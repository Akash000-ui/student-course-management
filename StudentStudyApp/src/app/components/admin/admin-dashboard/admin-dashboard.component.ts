import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CourseService, Course } from '../../../services/course.service';
import { AuthService } from '../../../services/auth.service';
import { AdminService, AdminDashboardStats, CourseStats } from '../../../services/admin.service';
import { CategoryService, Category } from '../../../services/category.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-admin-dashboard',
  standalone: false,
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  courses: Course[] = [];
  loading = false;
  currentUser: any = null;
  statsLoading = false;

  // Categories
  categories: Category[] = [];
  categoryMap: Map<string, string> = new Map();

  // Statistics
  stats: AdminDashboardStats | null = null;
  totalCourses = 0;
  totalVideos = 0;
  totalStudents = 0;
  totalEnrollments = 0;
  newUsersThisMonth = 0;
  newEnrollmentsThisMonth = 0;

  constructor(
    private courseService: CourseService,
    private authService: AuthService,
    private adminService: AdminService,
    private categoryService: CategoryService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadCategories();
    this.loadCourses();
    this.loadStatistics();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (response) => {
        if (response.success) {
          this.categories = response.data;
          // Build category map for quick lookup
          this.categories.forEach(cat => {
            this.categoryMap.set(cat.id, cat.name);
          });
        }
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }

  getCategoryName(categoryId: string): string {
    return this.categoryMap.get(categoryId) || 'Unknown';
  }

  loadCourses(): void {
    this.loading = true;
    this.courseService.getAllCourses().subscribe({
      next: (response) => {
        if (response.success) {
          this.courses = response.data;
          this.totalCourses = this.courses.length;
        } else {
          this.showError('Failed to load courses');
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading courses:', error);
        this.showError('Error loading courses');
        this.loading = false;
      }
    });
  }

  loadStatistics(): void {
    this.statsLoading = true;
    this.adminService.getAdminStats().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.stats = response.data;
          this.totalCourses = response.data.totalCourses;
          this.totalVideos = response.data.totalVideos;
          this.totalStudents = response.data.totalUsers;
          this.totalEnrollments = response.data.totalEnrollments;
          this.newUsersThisMonth = response.data.newUsersThisMonth;
          this.newEnrollmentsThisMonth = response.data.newEnrollmentsThisMonth;
        } else {
          console.error('Failed to load statistics');
        }
        this.statsLoading = false;
      },
      error: (error) => {
        console.error('Error loading statistics:', error);
        this.statsLoading = false;
      }
    });
  }

  navigateToCreateCourse(): void {
    this.router.navigate(['/admin/create-course']);
  }

  editCourse(course: Course): void {
    this.router.navigate(['/admin/edit-course', course.id]);
  }

  manageCourseVideos(course: Course): void {
    this.router.navigate(['/admin/courses', course.id, 'videos']);
  }

  deleteCourse(course: Course): void {
    if (confirm(`Are you sure you want to delete the course "${course.title}"?`)) {
      this.courseService.deleteCourse(course.id).subscribe({
        next: (response) => {
          if (response.success) {
            this.showSuccess('Course deleted successfully');
            this.loadCourses(); // Reload courses
          } else {
            this.showError('Failed to delete course');
          }
        },
        error: (error) => {
          console.error('Error deleting course:', error);
          this.showError('Error deleting course');
        }
      });
    }
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['error-snackbar']
    });
  }
}
