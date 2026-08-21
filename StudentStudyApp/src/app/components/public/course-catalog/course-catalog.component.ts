import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { CategoryService, Category } from '../../../services/category.service';
import { CourseService, Course } from '../../../services/course.service';

@Component({
  selector: 'app-course-catalog',
  standalone: false,
  templateUrl: './course-catalog.component.html',
  styleUrl: './course-catalog.component.css'
})
export class CourseCatalogComponent implements OnInit {
  @ViewChild('courseRail') courseRail?: ElementRef<HTMLDivElement>;

  courses: Course[] = [];
  categoryMap = new Map<string, string>();
  loading = false;
  error = '';

  private courseVisuals = [
    { keywords: ['java', 'spring'], image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original.svg', theme: 'course-java' },
    { keywords: ['python', 'django', 'flask'], image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/python/python-original.svg', theme: 'course-python' },
    { keywords: ['android', 'mobile'], image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/android/android-original.svg', theme: 'course-android' },
    { keywords: ['web', 'angular', 'react', 'mern', 'mean', 'node', 'full stack', 'frontend'], image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/react/react-original.svg', theme: 'course-web' },
    { keywords: ['ai', 'artificial', 'machine', 'learning', 'deep', 'vision', 'tensorflow'], image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/tensorflow/tensorflow-original.svg', theme: 'course-ai' },
    { keywords: ['data', 'analytics', 'science', 'sql', 'mysql'], image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/mysql/mysql-original.svg', theme: 'course-data' }
  ];

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
        if (response.success && response.data) {
          this.categoryMap.clear();
          response.data.forEach((category: Category) => this.categoryMap.set(category.id, category.name));
        }
      }
    });
  }

  loadCourses(): void {
    this.loading = true;
    this.error = '';

    this.courseService.getAllCourses().subscribe({
      next: (response) => {
        this.loading = false;
        this.courses = response.success && response.data ? response.data : [];
      },
      error: () => {
        this.loading = false;
        this.error = 'Failed to load courses. Please try again later.';
      }
    });
  }

  scrollCourses(direction: 'left' | 'right'): void {
    this.courseRail?.nativeElement.scrollBy({
      left: direction === 'right' ? 420 : -420,
      behavior: 'smooth'
    });
  }

  viewCourse(courseId: string): void {
    this.router.navigate(['/courses/preview', courseId]);
  }

  getCategoryName(categoryId: string): string {
    return this.categoryMap.get(categoryId) || 'Course';
  }

  getCourseImage(course: Course): string {
    return course.thumbnailUrl?.trim() || this.getCourseVisual(course).image;
  }

  getCourseTheme(course: Course): string {
    return this.getCourseVisual(course).theme;
  }

  private getCourseVisual(course: Course): { image: string; theme: string } {
    const text = `${course.title || ''} ${course.description || ''} ${this.getCategoryName(course.categoryId)}`.toLowerCase();
    return this.courseVisuals.find(visual => visual.keywords.some(keyword => text.includes(keyword))) || {
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/vscode/vscode-original.svg',
      theme: 'course-default'
    };
  }
}
