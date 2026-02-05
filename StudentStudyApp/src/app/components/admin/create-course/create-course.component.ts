import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CourseService, CreateCourseRequest } from '../../../services/course.service';
import { CategoryService, Category } from '../../../services/category.service';

@Component({
  selector: 'app-create-course',
  standalone: false,
  templateUrl: './create-course.component.html',
  styleUrl: './create-course.component.css'
})
export class CreateCourseComponent implements OnInit {
  courseForm!: FormGroup;
  loading = false;
  loadingCategories = false;

  categories: Category[] = [];

  difficulties = [
    'BEGINNER',
    'INTERMEDIATE',
    'ADVANCED'
  ];

  languages = [
    'ENGLISH',
    'HINDI',
    'TELUGU'
  ];

  constructor(
    private fb: FormBuilder,
    private courseService: CourseService,
    private categoryService: CategoryService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.initializeForm();
    this.loadCategories();
  }

  private loadCategories(): void {
    this.loadingCategories = true;
    this.categoryService.getAllCategories().subscribe({
      next: (response) => {
        this.loadingCategories = false;
        if (response.success) {
          this.categories = response.data;
        } else {
          this.showError('Failed to load categories');
        }
      },
      error: (error) => {
        this.loadingCategories = false;
        console.error('Error loading categories:', error);
        this.showError('Error loading categories');
      }
    });
  }

  private initializeForm(): void {
    this.courseForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10)]], // Removed maxLength limitation
      categoryId: ['', Validators.required],
      difficulty: ['', Validators.required],
      thumbnailUrl: [''],

      // Trainer Information
      trainerName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      trainerBio: ['', [Validators.required, Validators.minLength(10)]],
      experience: ['', Validators.required],
      linkedinProfile: [''],
      fieldOfWork: ['', Validators.required],
      profilePictureUrl: [''],
      language: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.courseForm.valid) {
      this.loading = true;

      const courseData: CreateCourseRequest = {
        title: this.courseForm.value.title,
        description: this.courseForm.value.description,
        categoryId: this.courseForm.value.categoryId,
        difficulty: this.courseForm.value.difficulty,
        thumbnailUrl: this.courseForm.value.thumbnailUrl || undefined,

        // Trainer Information
        trainerName: this.courseForm.value.trainerName,
        trainerBio: this.courseForm.value.trainerBio,
        experience: this.courseForm.value.experience,
        linkedinProfile: this.courseForm.value.linkedinProfile || undefined,
        fieldOfWork: this.courseForm.value.fieldOfWork,
        profilePictureUrl: this.courseForm.value.profilePictureUrl || undefined,
        language: this.courseForm.value.language
      };

      this.courseService.createCourse(courseData).subscribe({
        next: (response) => {
          this.loading = false;
          if (response.success) {
            this.showSuccess('Course created successfully!');
            this.router.navigate(['/admin']);
          } else {
            this.showError(response.message || 'Failed to create course');
          }
        },
        error: (error) => {
          this.loading = false;
          console.error('Error creating course:', error);
          this.showError(error.error?.message || 'Error creating course');
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel(): void {
    this.router.navigate(['/admin']);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.courseForm.controls).forEach(key => {
      const control = this.courseForm.get(key);
      control?.markAsTouched();
    });
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
