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
  courseFamilies = [
    {
      title: 'Java Full Stack',
      tag: 'Backend + UI',
      description: 'Spring Boot, Angular, REST APIs, SQL, deployment, and live project practice.',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original.svg',
      theme: 'java'
    },
    {
      title: 'Python Full Stack',
      tag: 'Apps + automation',
      description: 'Python programming, web apps, data handling, APIs, and guided implementation.',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/python/python-original.svg',
      theme: 'python'
    },
    {
      title: 'Data Science',
      tag: 'Analytics track',
      description: 'Statistics, machine learning, visualization, project datasets, and reporting.',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/tensorflow/tensorflow-original.svg',
      theme: 'ai'
    },
    {
      title: 'Artificial Intelligence',
      tag: 'AI systems',
      description: 'Model training concepts, ML workflows, computer vision, and AI project ideas.',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/pytorch/pytorch-original.svg',
      theme: 'ai'
    },
    {
      title: 'Web Technologies',
      tag: 'Frontend + server',
      description: 'HTML, CSS, JavaScript, Angular, React, Node.js, MongoDB, and projects.',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/angular/angular-original.svg',
      theme: 'web'
    },
    {
      title: 'MERN / MEAN Stack',
      tag: 'Full stack',
      description: 'React or Angular with Node.js, Express, MongoDB, authentication, and deployment.',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/nodejs/nodejs-original.svg',
      theme: 'web'
    },
    {
      title: 'Data Analytics',
      tag: 'BI skills',
      description: 'Excel, SQL, dashboards, analytics thinking, reporting, and business datasets.',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/mysql/mysql-original.svg',
      theme: 'data'
    },
    {
      title: 'SAP and Tally',
      tag: 'Enterprise tools',
      description: 'Business process, accounting workflows, enterprise basics, and job-oriented practice.',
      image: 'https://cdn.simpleicons.org/sap/0FAAFF',
      theme: 'enterprise'
    }
  ];

  private courseVisuals = [
    {
      keywords: ['java', 'spring'],
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original.svg',
      theme: 'course-java'
    },
    {
      keywords: ['python', 'django', 'flask'],
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/python/python-original.svg',
      theme: 'course-python'
    },
    {
      keywords: ['ai', 'artificial', 'machine', 'learning', 'deep', 'vision', 'tensorflow'],
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/tensorflow/tensorflow-original.svg',
      theme: 'course-ai'
    },
    {
      keywords: ['data', 'analytics', 'science', 'sql', 'mysql'],
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/mysql/mysql-original.svg',
      theme: 'course-data'
    },
    {
      keywords: ['web', 'angular', 'react', 'mern', 'mean', 'node', 'full stack', 'frontend'],
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/react/react-original.svg',
      theme: 'course-web'
    },
    {
      keywords: ['android', 'mobile'],
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/android/android-original.svg',
      theme: 'course-android'
    }
  ];

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

  getCourseImage(course: Course): string {
    if (course.thumbnailUrl) {
      return course.thumbnailUrl;
    }

    return this.getCourseVisual(course).image;
  }

  getCourseTheme(course: Course): string {
    return this.getCourseVisual(course).theme;
  }

  private getCourseVisual(course: Course): { image: string; theme: string } {
    const text = `${course.title || ''} ${course.description || ''} ${this.getCategoryName(course.categoryId) || ''}`.toLowerCase();
    const visual = this.courseVisuals.find(item => item.keywords.some(keyword => text.includes(keyword)));
    return visual || {
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/vscode/vscode-original.svg',
      theme: 'course-default'
    };
  }
}
