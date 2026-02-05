import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CategoryService, Category, CategoryRequest } from '../../../services/category.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-category-management',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './category-management.component.html',
  styleUrl: './category-management.component.css'
})
export class CategoryManagementComponent implements OnInit {
  categories: Category[] = [];
  loading = false;

  // Dialog state
  showDialog = false;
  dialogMode: 'create' | 'edit' = 'create';
  categoryForm!: FormGroup;
  selectedCategory: Category | null = null;

  // Table columns
  displayedColumns: string[] = ['name', 'description', 'active', 'createdAt', 'actions'];

  constructor(
    private categoryService: CategoryService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.initializeForm();
    this.loadCategories();
  }

  private initializeForm(): void {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['']
    });
  }

  loadCategories(): void {
    this.loading = true;
    this.categoryService.getAllCategoriesForAdmin().subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.categories = response.data;
        } else {
          this.showError('Failed to load categories');
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error loading categories:', error);
        this.showError('Error loading categories');
      }
    });
  }

  openCreateDialog(): void {
    this.dialogMode = 'create';
    this.selectedCategory = null;
    this.categoryForm.reset();
    this.showDialog = true;
  }

  openEditDialog(category: Category): void {
    this.dialogMode = 'edit';
    this.selectedCategory = category;
    this.categoryForm.patchValue({
      name: category.name,
      description: category.description || ''
    });
    this.showDialog = true;
  }

  closeDialog(): void {
    this.showDialog = false;
    this.categoryForm.reset();
    this.selectedCategory = null;
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    const request: CategoryRequest = {
      name: this.categoryForm.value.name.trim(),
      description: this.categoryForm.value.description?.trim() || undefined
    };

    if (this.dialogMode === 'create') {
      this.createCategory(request);
    } else if (this.selectedCategory) {
      this.updateCategory(this.selectedCategory.id, request);
    }
  }

  private createCategory(request: CategoryRequest): void {
    this.loading = true;
    this.categoryService.createCategory(request).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.showSuccess('Category created successfully');
          this.closeDialog();
          this.loadCategories();
        } else {
          this.showError(response.message || 'Failed to create category');
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error creating category:', error);
        this.showError(error.error?.message || 'Error creating category');
      }
    });
  }

  private updateCategory(id: string, request: CategoryRequest): void {
    this.loading = true;
    this.categoryService.updateCategory(id, request).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.showSuccess('Category updated successfully');
          this.closeDialog();
          this.loadCategories();
        } else {
          this.showError(response.message || 'Failed to update category');
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error updating category:', error);
        this.showError(error.error?.message || 'Error updating category');
      }
    });
  }

  deleteCategory(category: Category): void {
    if (confirm(`Are you sure you want to delete "${category.name}"? This will fail if any courses use this category.`)) {
      this.loading = true;
      this.categoryService.deleteCategory(category.id).subscribe({
        next: (response) => {
          this.loading = false;
          if (response.success) {
            this.showSuccess('Category deleted successfully');
            this.loadCategories();
          } else {
            this.showError(response.message || 'Failed to delete category');
          }
        },
        error: (error) => {
          this.loading = false;
          console.error('Error deleting category:', error);
          this.showError(error.error?.message || 'Error deleting category');
        }
      });
    }
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
}
