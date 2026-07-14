import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseService, Course } from '../../../services/course.service';
import { CategoryService } from '../../../services/category.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-course-preview',
  standalone: false,
  templateUrl: './course-preview.component.html',
  styleUrl: './course-preview.component.css'
})
export class CoursePreviewComponent implements OnInit {
  course: Course | null = null;
  loading = false;
  error = '';
  courseId = '';
  isDescriptionExpanded = false;
  isTrainerBioExpanded = false;
  categoryName = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private categoryService: CategoryService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.courseId = this.route.snapshot.paramMap.get('id') || '';
    if (this.courseId) {
      this.loadCourse();
    } else {
      this.router.navigate(['/courses']);
    }
  }

  loadCourse(): void {
    this.loading = true;
    this.error = '';

    this.courseService.getCourseById(this.courseId).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.course = response.data;
          // Load category name
          if (this.course.categoryId) {
            this.loadCategoryName(this.course.categoryId);
          }
        } else {
          this.error = 'Course not found';
        }
      },
      error: (error) => {
        this.loading = false;
        this.error = 'Failed to load course details';
        console.error('Error loading course:', error);
      }
    });
  }

  loadCategoryName(categoryId: string): void {
    this.categoryService.getCategoryById(categoryId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.categoryName = response.data.name;
        }
      },
      error: (error) => {
        console.error('Error loading category:', error);
        this.categoryName = 'Unknown';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/courses']);
  }

  navigateToSignIn(): void {
    this.snackBar.open('Please sign in to enroll in this course', 'Close', {
      duration: 3000,
      panelClass: ['info-snackbar']
    });
    this.router.navigate(['/signin']);
  }

  navigateToSignUp(): void {
    this.router.navigate(['/signup']);
  }

  toggleDescription(): void {
    this.isDescriptionExpanded = !this.isDescriptionExpanded;
  }

  toggleTrainerBio(): void {
    this.isTrainerBioExpanded = !this.isTrainerBioExpanded;
  }

  shouldShowReadMore(text: string, limit: number = 200): boolean {
    return text ? text.length > limit : false;
  }
}
