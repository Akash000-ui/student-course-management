import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { Subscription, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { CourseService, Course } from '../../../services/course.service';
import { EnrollmentService } from '../../../services/enrollment.service';
import { Enrollment } from '../../../models/enrollment.model';
import { AuthService } from '../../../services/auth.service';
import { CategoryService, Category } from '../../../services/category.service';

@Component({
  selector: 'app-user-dashboard',
  standalone: false,
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})
export class UserDashboardComponent implements OnInit, OnDestroy {
  currentUser: any = null;
  allCourses: Course[] = [];
  filteredCourses: Course[] = [];
  paginatedCourses: Course[] = [];
  enrollments: Enrollment[] = [];
  recentEnrollments: Enrollment[] = [];

  // Search and Filter
  searchQuery = '';
  selectedCategory = '';
  selectedDifficulty = '';
  selectedSortBy = 'latest'; // Default sort by latest
  categories: Category[] = [];
  categoryMap: Map<string, string> = new Map(); // Map categoryId to category name

  // View Mode
  viewMode: 'grid' | 'list' = 'grid';

  // Pagination
  pageSize = 12;
  currentPage = 0;

  // Loading state
  loading = false;

  // Stats
  enrolledCoursesCount = 0;
  completedCoursesCount = 0;

  // Dialog for showing courses
  showCoursesDialog = false;
  dialogTitle = '';
  dialogType: 'enrolled' | 'completed' = 'enrolled';
  dialogCourses: Enrollment[] = [];

  // Search debounce
  private searchSubject = new Subject<string>();
  private subscriptions: Subscription[] = [];

  constructor(
    private courseService: CourseService,
    private enrollmentService: EnrollmentService,
    private authService: AuthService,
    private categoryService: CategoryService,
    private snackBar: MatSnackBar,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.setupSearchDebounce();
    this.loadCategories();
    this.loadCourses();
    this.loadEnrollments(); // This will call loadUserStats() after data is loaded
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  private loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (response) => {
        if (response.success) {
          this.categories = response.data;
          // Build category map for quick lookup
          this.categories.forEach(cat => {
            console.log('Mapping category:', cat.id, 'to', cat.name);
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

  private setupSearchDebounce(): void {
    const searchSub = this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.loadCourses(); // Reload courses with new search term
    });
    this.subscriptions.push(searchSub);
  }

  loadCourses(): void {
    this.loading = true;

    // Prepare filters for backend request
    const filters = {
      category: this.selectedCategory || undefined,
      difficulty: this.selectedDifficulty || undefined,
      search: this.searchQuery.trim() || undefined
    };

    const coursesSub = this.courseService.getAllCourses(filters).subscribe({
      next: (response) => {
        if (response.success) {
          this.allCourses = response.data;
          this.filteredCourses = response.data; // Since filtering is done on backend
          this.sortCourses(); // Apply sorting
          this.updatePaginatedCourses();
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
    this.subscriptions.push(coursesSub);
  }

  sortCourses(): void {
    switch (this.selectedSortBy) {
      case 'latest':
        // Sort by createdAt descending (newest first)
        this.filteredCourses.sort((a, b) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA; // Descending order
        });
        break;
      case 'oldest':
        // Sort by createdAt ascending (oldest first)
        this.filteredCourses.sort((a, b) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateA - dateB; // Ascending order
        });
        break;
      case 'title-asc':
        // Sort by title A-Z
        this.filteredCourses.sort((a, b) => a.title.localeCompare(b.title));
        break;
      case 'title-desc':
        // Sort by title Z-A
        this.filteredCourses.sort((a, b) => b.title.localeCompare(a.title));
        break;
      default:
        // Default to latest
        this.filteredCourses.sort((a, b) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA;
        });
    }
  }

  onSortChange(): void {
    this.sortCourses();
    this.currentPage = 0; // Reset to first page
    this.updatePaginatedCourses();
  }

  private loadUserStats(): void {
    // Calculate stats from current enrollments reflecting id-based progress
    const valid = this.getValidEnrollments(this.enrollments);
    this.enrolledCoursesCount = valid.filter(e => !(e.progressPercentage === 100 || e.isCompleted)).length;
    this.completedCoursesCount = valid.filter(e => (e.progressPercentage === 100 || e.isCompleted)).length;
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchQuery);
  }

  onFilterChange(): void {
    this.currentPage = 0; // Reset to first page
    this.loadCourses(); // Reload courses with new filters
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedCategory = '';
    this.selectedDifficulty = '';
    this.selectedSortBy = 'latest'; // Reset to default sort
    this.currentPage = 0;
    this.loadCourses(); // Reload courses without filters
  }

  updatePaginatedCourses(): void {
    const startIndex = this.currentPage * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedCourses = this.filteredCourses.slice(startIndex, endIndex);
  }

  setViewMode(mode: 'grid' | 'list'): void {
    this.viewMode = mode;
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updatePaginatedCourses();
  }

  onCourseClick(courseOrEnrollment: Course | Enrollment): void {
    let courseId: string;

    // Check if it's an enrollment or a course
    if ('courseId' in courseOrEnrollment) {
      // It's an enrollment
      courseId = courseOrEnrollment.courseId;
    } else {
      // It's a course
      courseId = courseOrEnrollment.id;
    }

    // Navigate to course details
    this.router.navigate(['/course', courseId]);
  }

  onEnrollClick(course: Course): void {
    const request = { courseId: course.id };

    this.enrollmentService.enrollInCourse(request).subscribe({
      next: (response) => {
        this.snackBar.open(`Successfully enrolled in ${course.title}!`, 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.loadEnrollments(); // Refresh enrollments and stats
        this.router.navigate(['/course', course.id]); // Navigate to course
      },
      error: (error) => {
        const message = error.error?.message || 'Failed to enroll in course';
        this.showError(message);
      }
    });
  }

  /**
   * Load user enrollments
   */
  loadEnrollments(): void {
    this.enrollmentService.getUserEnrollments().subscribe({
      next: (response) => {
        // Filter out clearly invalid/enriched-null entries first
        this.enrollments = (response.data || []).filter(e => !!e && !!e.courseId);
        // Merge id-based progress per enrollment to reflect true state
        this.refreshProgressForEnrollments();
      },
      error: (error) => {
        console.error('Error loading enrollments:', error);
      }
    });

    // Load recent enrollments for quick access
    this.enrollmentService.getRecentEnrollments().subscribe({
      next: (response) => {
        this.recentEnrollments = response.data;
      },
      error: (error) => {
        console.error('Error loading recent enrollments:', error);
      }
    });
  }

  /**
   * Check if user is enrolled in a course
   */
  isEnrolled(courseId: string): boolean {
    return this.enrollments.some(enrollment => enrollment.courseId === courseId);
  }

  /**
   * Get enrollment for a course
   */
  getEnrollment(courseId: string): Enrollment | null {
    return this.enrollments.find(enrollment => enrollment.courseId === courseId) || null;
  }

  /**
   * Get enrollment progress color
   */
  getProgressColor(enrollment: Enrollment): string {
    return this.enrollmentService.getProgressColor(enrollment.progressPercentage);
  }

  /**
   * Show enrolled courses dialog
   */
  showEnrolledCourses(): void {
    this.dialogType = 'enrolled';
    this.dialogTitle = 'Your Enrolled Courses';
    const valid = this.getValidEnrollments(this.enrollments);
    this.dialogCourses = valid.filter(enrollment => (enrollment.progressPercentage || 0) < 100 && !enrollment.isCompleted);
    this.showCoursesDialog = true;
  }

  /**
   * Show completed courses dialog
   */
  showCompletedCourses(): void {
    this.dialogType = 'completed';
    this.dialogTitle = 'Your Completed Courses';
    const valid = this.getValidEnrollments(this.enrollments);
    this.dialogCourses = valid.filter(enrollment => (enrollment.progressPercentage || 0) === 100 || enrollment.isCompleted);
    this.showCoursesDialog = true;
  }

  /**
   * Close courses dialog
   */
  closeCoursesDialog(): void {
    this.showCoursesDialog = false;
    this.dialogCourses = [];
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['error-snackbar']
    });
  }

  // ===== Helpers: progress + validity =====
  private getValidEnrollments(items: Enrollment[]): Enrollment[] {
    // Filter out invalid enrollments (missing course metadata = deleted course)
    return (items || []).filter(e => !!e && !!e.courseId && e.courseTitle != null);
  }

  private refreshProgressForEnrollments(): void {
    // ✅ SIMPLIFIED: Backend now sends accurate progress, no need to recalculate
    // Progress is calculated dynamically from UserVideoCompletion in backend
    this.loadUserStats();
  }
}
