import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Enrollment, EnrollmentRequest, EnrollmentStats, ApiResponse } from '../models/enrollment.model';
import { ApiResponse as ProgressApiResponse, VideoCompletionResponse } from '../models/progress.model';

@Injectable({
  providedIn: 'root'
})
export class EnrollmentService {
  // private baseUrl = 'https://studiehub-backend-latest.onrender.com/api/enrollments';
  private baseUrl = 'http://localhost:8080/api/enrollments';

  constructor(private http: HttpClient) { }

  /**
   * Enroll in a course
   */
  enrollInCourse(request: EnrollmentRequest): Observable<ApiResponse<Enrollment>> {
    return this.http.post<ApiResponse<Enrollment>>(this.baseUrl, request);
  }

  /**
   * Get all enrollments for current user
   */
  getUserEnrollments(): Observable<ApiResponse<Enrollment[]>> {
    return this.http.get<ApiResponse<Enrollment[]>>(this.baseUrl);
  }

  /**
   * Get specific enrollment by course ID
   */
  getEnrollmentByCourse(courseId: string): Observable<ApiResponse<Enrollment>> {
    return this.http.get<ApiResponse<Enrollment>>(`${this.baseUrl}/course/${courseId}`);
  }

  /**
   * Access a course (update last accessed time)
   */
  accessCourse(courseId: string): Observable<ApiResponse<Enrollment>> {
    return this.http.put<ApiResponse<Enrollment>>(`${this.baseUrl}/course/${courseId}/access`, {});
  }

  /**
   * Get completed enrollments for current user
   */
  getCompletedEnrollments(): Observable<ApiResponse<Enrollment[]>> {
    return this.http.get<ApiResponse<Enrollment[]>>(`${this.baseUrl}/completed`);
  }

  /**
   * Get recent enrollments for dashboard
   */
  getRecentEnrollments(): Observable<ApiResponse<Enrollment[]>> {
    return this.http.get<ApiResponse<Enrollment[]>>(`${this.baseUrl}/recent`);
  }

  /**
   * Check if user is enrolled in a course
   */
  checkEnrollment(courseId: string): Observable<ApiResponse<boolean>> {
    return this.http.get<ApiResponse<boolean>>(`${this.baseUrl}/check/${courseId}`);
  }

  /**
   * Get enrollment statistics for current user
   */
  getEnrollmentStats(): Observable<ApiResponse<EnrollmentStats>> {
    return this.http.get<ApiResponse<EnrollmentStats>>(`${this.baseUrl}/stats`);
  }

  /**
   * Mark a video as complete and get progress (uses progress API)
   */
  markVideoComplete(courseId: string, videoId: string): Observable<ProgressApiResponse<VideoCompletionResponse>> {
    return this.http.put<ProgressApiResponse<VideoCompletionResponse>>(
      `http://localhost:8080/api/progress/courses/${courseId}/videos/${videoId}/complete`,
      {}
    );
  }

  /**
   * Get course progress (uses progress API)
   */
  getCourseProgress(courseId: string): Observable<ProgressApiResponse<VideoCompletionResponse>> {
    return this.http.get<ProgressApiResponse<VideoCompletionResponse>>(
      `http://localhost:8080/api/progress/courses/${courseId}`
    );
  }

  /**
   * Calculate progress percentage
   */
  calculateProgress(completedVideos: number, totalVideos: number): number {
    if (totalVideos === 0) return 0;
    return Math.round((completedVideos / totalVideos) * 100);
  }

  /**
   * Format enrollment status
   */
  getEnrollmentStatus(enrollment: Enrollment): string {
    if (enrollment.isCompleted) {
      return 'Completed';
    } else if (enrollment.progressPercentage > 0) {
      return 'In Progress';
    } else {
      return 'Not Started';
    }
  }

  /**
   * Get progress color based on percentage
   */
  getProgressColor(progressPercentage: number): string {
    if (progressPercentage === 100) {
      return 'primary';
    } else if (progressPercentage >= 50) {
      return 'accent';
    } else if (progressPercentage > 0) {
      return 'warn';
    } else {
      return 'basic';
    }
  }

  /**
   * Format time since enrollment
   */
  getTimeSinceEnrollment(enrolledAt: string): string {
    const enrollmentDate = new Date(enrolledAt);
    const now = new Date();
    const diffInMs = now.getTime() - enrollmentDate.getTime();
    const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));

    if (diffInDays === 0) {
      return 'Today';
    } else if (diffInDays === 1) {
      return 'Yesterday';
    } else if (diffInDays < 7) {
      return `${diffInDays} days ago`;
    } else if (diffInDays < 30) {
      const weeks = Math.floor(diffInDays / 7);
      return `${weeks} week${weeks > 1 ? 's' : ''} ago`;
    } else {
      const months = Math.floor(diffInDays / 30);
      return `${months} month${months > 1 ? 's' : ''} ago`;
    }
  }
}
