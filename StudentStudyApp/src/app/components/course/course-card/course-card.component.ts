import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Course } from '../../../services/course.service';
import { Enrollment } from '../../../models/enrollment.model';

@Component({
  selector: 'app-course-card',
  standalone: false,
  templateUrl: './course-card.component.html',
  styleUrl: './course-card.component.css'
})
export class CourseCardComponent {
  @Input() course: Course | null = null;
  @Input() enrollment: Enrollment | null = null; // Add enrollment input
  @Input() categoryName: string = ''; // Category name for display
  @Output() courseClick = new EventEmitter<Course>();
  @Output() enrollClick = new EventEmitter<Course>();
  imageError = false;

  // Helper methods for enrollment status
  get isEnrolled(): boolean {
    return this.enrollment !== null && this.enrollment !== undefined;
  }

  get isCompleted(): boolean {
    return this.isEnrolled && (this.enrollment!.isCompleted || this.enrollment!.progressPercentage === 100);
  }

  get isInProgress(): boolean {
    return this.isEnrolled && !this.isCompleted;
  }

  get progressPercentage(): number {
    return this.enrollment?.progressPercentage || 0;
  }

  getEnrollmentStatus(): string {
    if (this.isCompleted) return 'Completed';
    if (this.isInProgress) return 'In Progress';
    return 'Not Enrolled';
  }

  getButtonText(): string {
    if (this.isCompleted) return 'View Course';
    if (this.isInProgress) return 'Continue Learning';
    return 'Enroll Now';
  }

  getButtonIcon(): string {
    if (this.isCompleted) return 'check_circle';
    if (this.isInProgress) return 'play_circle';
    return 'school';
  }

  onCourseClick(): void {
    if (this.course) {
      this.courseClick.emit(this.course);
    }
  }

  onEnrollClick(event: Event): void {
    event.stopPropagation(); // Prevent card click
    if (this.course) {
      // If already enrolled, just navigate to course (same as card click)
      if (this.isEnrolled) {
        this.courseClick.emit(this.course);
      } else {
        // Otherwise, emit enroll event
        this.enrollClick.emit(this.course);
      }
    }
  }

  getThumbnailUrl(): string | null {
    if (!this.course) return null;
    const url = this.course.thumbnailUrl?.trim() || '';
    return url.length > 0 ? url : null;
  }

  onImageError(_e: Event): void {
    this.imageError = true;
  }

  getDifficultyColor(difficulty: string | undefined): string {
    if (!difficulty) return 'primary';
    switch (difficulty.toLowerCase()) {
      case 'beginner': return 'primary';
      case 'intermediate': return 'accent';
      case 'advanced': return 'warn';
      default: return 'primary';
    }
  }

  getPlaceholderColor(category: string | undefined): string {
    const categoryKey = this.categoryName || category || '';
    const colors: { [key: string]: string } = {
      'programming': '#3f51b5',
      'data science': '#ff9800',
      'design': '#e91e63',
      'business': '#4caf50',
      'languages': '#9c27b0',
      'photography': '#ff5722',
      'web development': '#2196f3',
      'mobile development': '#4caf50',
      'cloud computing': '#00bcd4',
      'cybersecurity': '#f44336'
    };
    return colors[categoryKey.toLowerCase()] || '#607d8b';
  }

  getPlaceholderGradient(category: string | undefined): string {
    const categoryKey = this.categoryName || category || '';
    const gradients: { [key: string]: string } = {
      'programming': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      'data science': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
      'design': 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
      'business': 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
      'languages': 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
      'photography': 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
      'web development': 'linear-gradient(135deg, #2196f3 0%, #1976d2 100%)',
      'mobile development': 'linear-gradient(135deg, #4caf50 0%, #388e3c 100%)',
      'cloud computing': 'linear-gradient(135deg, #00bcd4 0%, #0097a7 100%)',
      'cybersecurity': 'linear-gradient(135deg, #f44336 0%, #d32f2f 100%)'
    };
    return gradients[categoryKey.toLowerCase()] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
  }

  getCategoryIcon(category: string | undefined): string {
    const categoryKey = this.categoryName || category || '';
    const icons: { [key: string]: string } = {
      'programming': 'code',
      'data science': 'analytics',
      'design': 'palette',
      'business': 'business',
      'languages': 'language',
      'photography': 'camera_alt',
      'web development': 'web',
      'mobile development': 'phone_android',
      'cloud computing': 'cloud',
      'cybersecurity': 'security'
    };
    return icons[categoryKey.toLowerCase()] || 'book';
  }
}
