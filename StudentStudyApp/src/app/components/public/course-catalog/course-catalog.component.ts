import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CourseService, Course } from '../../../services/course.service';
import { CategoryService, Category } from '../../../services/category.service';

@Component({
  selector: 'app-course-catalog',
  standalone: false,
  templateUrl: './course-catalog.component.html',
  styleUrl: './course-catalog.component.css'
})
export class CourseCatalogComponent implements OnInit {
  courses: Course[] = [];
  filteredCourses: Course[] = [];
  loading = false;
  error = '';
  expandedDescriptions: Set<string> = new Set(); // Track which descriptions are expanded

  // Filter options
  categories: Category[] = [];
  categoryMap: Map<string, string> = new Map();
  difficulties: string[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];

  // Selected filters
  selectedCategory = '';
  selectedDifficulty = '';
  selectedSortBy = 'latest'; // Default sort by latest
  searchTerm = '';

  constructor(
    private courseService: CourseService,
    private categoryService: CategoryService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCategories();
    this.loadCourses();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (response) => {
        if (response.success) {
          this.categories = response.data;
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
    this.error = '';

    const filters = {
      category: this.selectedCategory,
      difficulty: this.selectedDifficulty,
      search: this.searchTerm
    };

    this.courseService.getAllCourses(filters).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.courses = response.data;
          this.filteredCourses = response.data;
          this.sortCourses(); // Apply sorting after loading
        }
      },
      error: (error) => {
        this.loading = false;
        this.error = 'Failed to load courses. Please try again later.';
        console.error('Error loading courses:', error);
      }
    });
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
  }

  onFilterChange(): void {
    this.loadCourses();
  }

  onSearchChange(): void {
    // Debounce search - only search after user stops typing
    setTimeout(() => {
      this.loadCourses();
    }, 500);
  }

  clearFilters(): void {
    this.selectedCategory = '';
    this.selectedDifficulty = '';
    this.searchTerm = '';
    this.selectedSortBy = 'latest';
    this.loadCourses();
  }

  viewCourseDetails(courseId: string): void {
    this.router.navigate(['/courses/preview', courseId]);
  }

  navigateToSignIn(): void {
    this.router.navigate(['/signin']);
  }

  navigateToSignUp(): void {
    this.router.navigate(['/signup']);
  }

  toggleDescription(courseId: string, event: Event): void {
    event.stopPropagation(); // Prevent card click event
    if (this.expandedDescriptions.has(courseId)) {
      this.expandedDescriptions.delete(courseId);
    } else {
      this.expandedDescriptions.add(courseId);
    }
  }

  isDescriptionExpanded(courseId: string): boolean {
    return this.expandedDescriptions.has(courseId);
  }

  shouldShowReadMore(description: string): boolean {
    return description.length > 120;
  }
}
