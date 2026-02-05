import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CourseService, Course, UpdateCourseRequest } from '../../../services/course.service';
import { CategoryService, Category } from '../../../services/category.service';

@Component({
    selector: 'app-edit-course',
    standalone: false,
    templateUrl: './edit-course.component.html',
    styleUrl: './edit-course.component.css'
})
export class EditCourseComponent implements OnInit {
    courseForm!: FormGroup;
    loading = false;
    loadingCategories = false;
    courseId!: string;
    course!: Course;

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
        private route: ActivatedRoute,
        private router: Router,
        private snackBar: MatSnackBar
    ) { }

    ngOnInit(): void {
        this.initializeForm();
        this.courseId = this.route.snapshot.paramMap.get('id') || '';
        if (!this.courseId) {
            this.showError('Invalid course id');
            this.router.navigate(['/admin']);
            return;
        }
        this.loadCategories();
        this.loadCourse();
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

    private loadCourse(): void {
        this.loading = true;
        this.courseService.getCourseById(this.courseId).subscribe({
            next: (res) => {
                this.loading = false;
                if (!res.success) {
                    this.showError(res.message || 'Failed to load course');
                    this.router.navigate(['/admin']);
                    return;
                }
                this.course = res.data;
                this.courseForm.patchValue({
                    title: this.course.title,
                    description: this.course.description,
                    categoryId: this.course.categoryId,
                    difficulty: this.course.difficulty,
                    thumbnailUrl: this.course.thumbnailUrl || '',

                    // Trainer Information
                    trainerName: this.course.trainerName,
                    trainerBio: this.course.trainerBio,
                    experience: this.course.experience,
                    linkedinProfile: this.course.linkedinProfile || '',
                    fieldOfWork: this.course.fieldOfWork,
                    profilePictureUrl: this.course.profilePictureUrl || '',
                    language: this.course.language
                });
            },
            error: (err) => {
                this.loading = false;
                console.error('Error loading course', err);
                this.showError('Error loading course');
                this.router.navigate(['/admin']);
            }
        });
    }

    onSubmit(): void {
        if (this.courseForm.invalid) {
            this.courseForm.markAllAsTouched();
            return;
        }
        this.loading = true;
        const payload: UpdateCourseRequest = {
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
        this.courseService.updateCourse(this.courseId, payload).subscribe({
            next: (res) => {
                this.loading = false;
                if (res.success) {
                    this.showSuccess('Course updated successfully');
                    this.router.navigate(['/admin']);
                } else {
                    this.showError(res.message || 'Failed to update course');
                }
            },
            error: (err) => {
                this.loading = false;
                console.error('Error updating course', err);
                this.showError(err.error?.message || 'Error updating course');
            }
        });
    }

    onCancel(): void {
        this.router.navigate(['/admin']);
    }

    private showSuccess(message: string): void {
        this.snackBar.open(message, 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
    }

    private showError(message: string): void {
        this.snackBar.open(message, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
    }
}
