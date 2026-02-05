import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CourseService, Course } from '../../../services/course.service';
import { VideoService } from '../../../services/video.service';
import { Video } from '../../../models/video.model';
import { EnrollmentService } from '../../../services/enrollment.service';
import { VideoCompletionResponse } from '../../../models/progress.model';
import { Enrollment } from '../../../models/enrollment.model';
import { AuthService } from '../../../services/auth.service';
import { CategoryService } from '../../../services/category.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-course-detail',
  standalone: false,
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.css'
})
export class CourseDetailComponent implements OnInit, OnDestroy {
  courseId!: string;
  course: Course | null = null;
  videos: Video[] = [];
  selectedVideo: Video | null = null;
  safeVideoUrl: SafeResourceUrl | null = null;
  enrollment: Enrollment | null = null;
  isEnrolled = false;
  loading = false;
  enrolling = false;
  user: any = null;
  categoryName = '';
  private destroy$ = new Subject<void>();

  // Description expansion properties
  showFullDescription = false;
  descriptionLimit = 200;
  showFullTrainerBio = false;
  trainerBioLimit = 150;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private videoService: VideoService,
    private enrollmentService: EnrollmentService,
    private authService: AuthService,
    private categoryService: CategoryService,
    private sanitizer: DomSanitizer,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    // Subscribe to route param changes for proper navigation
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.courseId = params.get('id') || '';
      this.user = this.authService.getCurrentUser();

      if (this.courseId) {
        this.resetComponent();
        this.loadCourse();
        this.loadVideos();
        this.checkEnrollment();
      } else {
        this.router.navigate(['/dashboard']);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private resetComponent(): void {
    this.course = null;
    this.videos = [];
    this.selectedVideo = null;
    this.safeVideoUrl = null;
    this.loading = false;
    this.completedVideoIds = new Set<string>();
    this.progressTotals = { totalCompleted: 0, totalVideos: 0 };
  }

  private loadCourse(): void {
    this.loading = true;
    this.courseService.getCourseById(this.courseId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.course = response.data;
          // Load category name
          if (this.course.categoryId) {
            this.loadCategoryName(this.course.categoryId);
          }
        } else {
          this.showError('Course not found');
          this.router.navigate(['/dashboard']);
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error loading course:', error);
        this.showError('Error loading course');
        this.router.navigate(['/dashboard']);
      }
    });
  }

  private loadCategoryName(categoryId: string): void {
    this.categoryService.getCategoryById(categoryId).pipe(takeUntil(this.destroy$)).subscribe({
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

  private loadVideos(): void {
    this.videoService.getVideosByCourseId(this.courseId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          // Order by position ascending (nulls last), then by createdAt as tie-breaker
          this.videos = response.data.sort((a, b) => {
            const pa = a.position ?? Number.MAX_SAFE_INTEGER;
            const pb = b.position ?? Number.MAX_SAFE_INTEGER;
            if (pa !== pb) return pa - pb;
            return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
          });

          // Auto-select first video if available
          if (this.videos.length > 0) {
            this.selectVideo(this.videos[0]);
          }

          // Also load progress once videos are loaded
          this.loadProgress();
        }
      },
      error: (error) => {
        console.error('Error loading videos:', error);
        this.showError('Error loading videos');
      }
    });
  }

  selectVideo(video: Video): void {
    this.selectedVideo = video;
    this.safeVideoUrl = this.getSafeVideoUrl(video.videoUrl);
  }

  private getSafeVideoUrl(url: string): SafeResourceUrl {
    // Convert YouTube watch URL to embed URL
    let embedUrl = url;

    if (url.includes('youtube.com/watch?v=')) {
      const videoId = url.split('v=')[1].split('&')[0];
      embedUrl = `https://www.youtube.com/embed/${videoId}`;
    } else if (url.includes('youtu.be/')) {
      const videoId = url.split('youtu.be/')[1].split('?')[0];
      embedUrl = `https://www.youtube.com/embed/${videoId}`;
    }

    return this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
  }

  // Build YouTube thumbnail URL (hqdefault)
  getYouTubeThumbnailUrl(url: string): string | null {
    const id = this.videoService.getYouTubeVideoId(url);
    return id ? `https://img.youtube.com/vi/${id}/hqdefault.jpg` : null;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  /**
   * Check if user is enrolled in the course
   */
  checkEnrollment(): void {
    if (!this.user) return;

    this.enrollmentService.checkEnrollment(this.courseId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.isEnrolled = response.data;
          if (this.isEnrolled) {
            this.loadEnrollment();
          }
        },
        error: (error) => {
          console.error('Error checking enrollment:', error);
        }
      });
  }

  /**
   * Load enrollment details
   */
  loadEnrollment(): void {
    this.enrollmentService.getEnrollmentByCourse(this.courseId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.enrollment = response.data;
        },
        error: (error) => {
          console.error('Error loading enrollment:', error);
        }
      });
  }

  /**
   * Enroll in the course
   */
  enrollInCourse(): void {
    if (!this.user || this.enrolling) return;

    this.enrolling = true;
    const request = { courseId: this.courseId };

    this.enrollmentService.enrollInCourse(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.enrollment = response.data;
          this.isEnrolled = true;
          this.enrolling = false;
          this.showSuccess('Successfully enrolled in course!');

          // Access the course to update last accessed time
          this.accessCourse();
        },
        error: (error) => {
          this.enrolling = false;
          if (error.error?.message?.includes('already enrolled')) {
            this.showError('You are already enrolled in this course');
            this.isEnrolled = true;
            this.loadEnrollment();
          } else {
            this.showError(error.error?.message || 'Failed to enroll in course');
          }
        }
      });
  }

  /**
   * Access course (update last accessed time)
   */
  accessCourse(): void {
    if (!this.isEnrolled) return;

    this.enrollmentService.accessCourse(this.courseId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.enrollment = response.data;
        },
        error: (error) => {
          console.error('Error accessing course:', error);
        }
      });
  }

  /**
   * Mark video as completed
   */
  markVideoCompleted(): void {
    if (!this.isEnrolled || !this.selectedVideo) return;

    const videoId = this.selectedVideo.id;
    // Use enrollment service to reload everything after marking complete
    this.enrollmentService.markVideoComplete(this.courseId, videoId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          const data = res.data as VideoCompletionResponse;
          this.completedVideoIds = new Set<string>(data.completedVideoIds || []);
          this.progressTotals = { totalCompleted: data.totalCompleted || 0, totalVideos: data.totalVideos || this.videos.length };

          // Reload enrollment to get updated progress from backend
          this.loadEnrollment();

          this.showSuccess(data.alreadyCompleted ? 'Already completed' : 'Video marked as completed!');
        },
        error: (error) => {
          console.error('Error marking video as completed:', error);
          this.showError(error.error?.message || 'Failed to mark video as completed');
        }
      });
  }

  /**
   * Get enrollment progress color
   */
  getProgressColor(): string {
    if (!this.enrollment) return 'basic';
    return this.enrollmentService.getProgressColor(this.enrollment.progressPercentage);
  }

  /**
   * Get enrollment status text
   */
  getEnrollmentStatus(): string {
    if (!this.enrollment) return 'Not Enrolled';
    return this.enrollmentService.getEnrollmentStatus(this.enrollment);
  }

  /**
   * Get progress circle circumference
   */
  getProgressCircumference(): string {
    const radius = 34;
    const circumference = 2 * Math.PI * radius;
    return `${circumference} ${circumference}`;
  }

  /**
   * Get progress circle offset
   */
  getProgressOffset(): number {
    if (!this.enrollment) return 0;
    const radius = 34;
    const circumference = 2 * Math.PI * radius;
    const progress = this.enrollment.progressPercentage || 0;
    return circumference - (progress / 100) * circumference;
  }

  /**
   * Check if a video is completed
   * Note: This is a simplified implementation since we don't track individual video completion
   * We'll assume the first N videos are completed based on completedVideos count
   */
  isVideoCompleted(videoId: string): boolean {
    return this.completedVideoIds.has(videoId);
  }

  /**
   * Open Google Drive file in new tab
   */
  openGoogleDriveFile(driveUrl: string): void {
    if (driveUrl) {
      // Use the video service to handle the URL conversion and opening
      this.videoService.openGoogleDriveFile(driveUrl);
    }
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

  trackByVideoId(index: number, video: Video): string {
    return video.id;
  }

  // ===== Progress via /api/progress =====
  completedVideoIds: Set<string> = new Set<string>();
  progressTotals: { totalCompleted: number; totalVideos: number } = { totalCompleted: 0, totalVideos: 0 };

  private loadProgress(): void {
    if (!this.isEnrolled) return;
    this.enrollmentService.getCourseProgress(this.courseId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          const data = res.data as VideoCompletionResponse;
          this.completedVideoIds = new Set<string>(data.completedVideoIds || []);
          this.progressTotals = { totalCompleted: data.totalCompleted || 0, totalVideos: data.totalVideos || this.videos.length };
        },
        error: (error) => {
          console.error('Error loading progress:', error);
        }
      });
  }

  // Helper methods for description expansion
  getDisplayDescription(): string {
    if (!this.course?.description) return '';
    if (this.showFullDescription || this.course.description.length <= this.descriptionLimit) {
      return this.course.description;
    }
    return this.course.description.substring(0, this.descriptionLimit) + '...';
  }

  shouldShowReadMore(): boolean {
    if (!this.course?.description) return false;
    return !this.showFullDescription && this.course.description.length > this.descriptionLimit;
  }

  shouldShowReadLess(): boolean {
    if (!this.course?.description) return false;
    return this.showFullDescription && this.course.description.length > this.descriptionLimit;
  }

  toggleDescription(): void {
    this.showFullDescription = !this.showFullDescription;
  }

  // Helper methods for trainer bio expansion
  getDisplayTrainerBio(): string {
    if (!this.course?.trainerBio) return '';
    if (this.showFullTrainerBio || this.course.trainerBio.length <= this.trainerBioLimit) {
      return this.course.trainerBio;
    }
    return this.course.trainerBio.substring(0, this.trainerBioLimit) + '...';
  }

  shouldShowTrainerBioReadMore(): boolean {
    if (!this.course?.trainerBio) return false;
    return !this.showFullTrainerBio && this.course.trainerBio.length > this.trainerBioLimit;
  }

  shouldShowTrainerBioReadLess(): boolean {
    if (!this.course?.trainerBio) return false;
    return this.showFullTrainerBio && this.course.trainerBio.length > this.trainerBioLimit;
  }

  toggleTrainerBio(): void {
    this.showFullTrainerBio = !this.showFullTrainerBio;
  }

  // Helper method for LinkedIn URL
  getLinkedInUrl(): string {
    if (!this.course?.linkedinProfile) return '';

    const url = this.course.linkedinProfile.trim();

    // If the URL already starts with http:// or https://, return as is
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }

    // If the URL starts with www., add https://
    if (url.startsWith('www.')) {
      return `https://${url}`;
    }

    // If it's just a path like linkedin.com/in/username, add https://
    if (url.startsWith('linkedin.com')) {
      return `https://${url}`;
    }

    // If it's just the username part, construct the full URL
    if (!url.includes('linkedin.com')) {
      return `https://www.linkedin.com/in/${url}`;
    }

    // Default fallback
    return `https://${url}`;
  }

  // Copy LinkedIn URL to clipboard
  copyLinkedInUrl(): void {
    const linkedInUrl = this.getLinkedInUrl();
    if (!linkedInUrl) return;

    // Use the modern Clipboard API if available
    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard.writeText(linkedInUrl).then(() => {
        this.snackBar.open('LinkedIn URL copied to clipboard!', 'Close', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom'
        });
      }).catch(err => {
        console.error('Failed to copy LinkedIn URL:', err);
        this.fallbackCopy(linkedInUrl);
      });
    } else {
      // Fallback for older browsers or non-secure contexts
      this.fallbackCopy(linkedInUrl);
    }
  }

  private fallbackCopy(text: string): void {
    // Create a temporary textarea element
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
      document.execCommand('copy');
      this.snackBar.open('LinkedIn URL copied to clipboard!', 'Close', {
        duration: 3000,
        horizontalPosition: 'center',
        verticalPosition: 'bottom'
      });
    } catch (err) {
      console.error('Fallback copy failed:', err);
      this.snackBar.open('Failed to copy URL. Please copy manually.', 'Close', {
        duration: 4000,
        horizontalPosition: 'center',
        verticalPosition: 'bottom'
      });
    } finally {
      document.body.removeChild(textArea);
    }
  }
}
