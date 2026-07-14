import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { VideoService } from '../../../services/video.service';
import { CourseService, Course } from '../../../services/course.service';
import { Video, VideoDto } from '../../../models/video.model';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-video-management',
  standalone: false,
  templateUrl: './video-management.component.html',
  styleUrl: './video-management.component.css'
})
export class VideoManagementComponent implements OnInit {
  courseId!: string;
  course: Course | null = null;
  videos: Video[] = [];
  loading = false;
  saving = false;
  showVideoForm = false;
  editingVideo: Video | null = null;

  videoForm!: FormGroup;
  // Dynamic arrays for managing multiple code files
  codeFileLinks: string[] = [];
  codeFileNames: string[] = [];

  // Getter to check if form can be submitted
  get canSubmit(): boolean {
    return this.videoForm && this.videoForm.valid && !this.saving;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private videoService: VideoService,
    private courseService: CourseService,
    private snackBar: MatSnackBar,
    private sanitizer: DomSanitizer
  ) { }

  ngOnInit(): void {
    this.courseId = this.route.snapshot.paramMap.get('courseId') || '';
    if (this.courseId) {
      this.initializeForm();
      this.loadCourse();
      this.loadVideos();
    } else {
      this.router.navigate(['/admin']);
    }
  }

  private initializeForm(): void {
    this.videoForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: [''], // Removed maxLength limitation
      videoUrl: ['', [Validators.required, this.youtubeUrlValidator.bind(this)]],
      position: [null],
      driveNotesFileLink: [''],
      driveNotesFileName: ['']
    });

    // Subscribe to form value changes to update validity
    this.videoForm.valueChanges.subscribe(() => {
      this.videoForm.updateValueAndValidity({ emitEvent: false });
    });
  }

  // Custom validator for YouTube URLs
  private youtubeUrlValidator(control: AbstractControl) {
    if (!control.value) {
      return null;
    }

    const youtubePattern = /^https?:\/\/(www\.)?(youtube\.com\/watch\?v=|youtu\.be\/)[a-zA-Z0-9_-]{11}.*$/;
    const isValid = youtubePattern.test(control.value);

    return isValid ? null : { invalidYouTubeUrl: true };
  }

  private loadCourse(): void {
    this.courseService.getCourseById(this.courseId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.course = response.data;
        } else {
          this.showError('Course not found');
          this.router.navigate(['/admin']);
        }
      },
      error: (error) => {
        console.error('Error loading course:', error);
        this.showError('Error loading course');
        this.router.navigate(['/admin']);
      }
    });
  }

  private loadVideos(): void {
    this.loading = true;
    this.videoService.getVideosByCourseId(this.courseId).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.videos = response.data.sort((a, b) => {
            const pa = (a.position ?? Number.MAX_SAFE_INTEGER);
            const pb = (b.position ?? Number.MAX_SAFE_INTEGER);
            if (pa !== pb) return pa - pb;
            return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
          });
        } else {
          this.showError('Failed to load videos');
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error loading videos:', error);
        this.showError('Error loading videos');
      }
    });
  }

  // TrackBy for stable DOM during re-render
  trackById(index: number, item: Video) {
    return item.id;
  }

  showCreateForm(): void {
    this.editingVideo = null;
    this.showVideoForm = true;
    this.videoForm.reset();

    // Initialize arrays for new video - start with empty arrays
    this.codeFileLinks = [];
    this.codeFileNames = [];

    // Default new video position to end of list
    this.videoForm.patchValue({
      position: (this.videos?.length || 0) + 1
    });
  }

  editVideo(video: Video): void {
    this.editingVideo = video;
    this.showVideoForm = true;

    // Initialize code file arrays
    this.codeFileLinks = video.driveCodeFileLinks ? [...video.driveCodeFileLinks] : [];
    this.codeFileNames = video.driveCodeFileNames ? [...video.driveCodeFileNames] : [];

    // Use setTimeout to ensure form is rendered before patching
    setTimeout(() => {
      // Only patch values that exist in the form
      this.videoForm.patchValue({
        title: video.title,
        description: video.description || '',
        videoUrl: video.videoUrl,
        position: video.position || (this.videos.findIndex(v => v.id === video.id) + 1),
        driveNotesFileLink: video.driveNotesFileLink || '',
        driveNotesFileName: video.driveNotesFileName || ''
      }, { emitEvent: true });

      // Force validation update
      Object.keys(this.videoForm.controls).forEach(key => {
        const control = this.videoForm.get(key);
        control?.updateValueAndValidity();
      });

      this.videoForm.updateValueAndValidity();

      // Debug logging
      console.log('=== Edit Video Debug ===');
      console.log('Form valid:', this.videoForm.valid);
      console.log('Form value:', this.videoForm.value);
      console.log('Form status:', this.videoForm.status);
      console.log('Individual field errors:');
      Object.keys(this.videoForm.controls).forEach(key => {
        const control = this.videoForm.get(key);
        if (control?.errors) {
          console.log(`  ${key}:`, control.errors);
        }
      });
    }, 0);
  }

  cancelForm(): void {
    this.showVideoForm = false;
    this.editingVideo = null;
    this.videoForm.reset();
    this.codeFileLinks = [];
    this.codeFileNames = [];
  }

  onSubmit(): void {
    if (this.videoForm.valid) {
      this.saving = true;

      // Ensure form arrays are updated with current values
      this.updateFormArrays();

      // Process code files - ensure matching arrays
      const validCodeFileEntries = this.codeFileLinks
        .map((link, index) => ({
          link: link.trim(),
          name: this.codeFileNames[index] ? this.codeFileNames[index].trim() : `Code File ${index + 1}`
        }))
        .filter(entry => entry.link !== ''); // Only include entries with valid links

      const videoData: VideoDto = {
        title: this.videoForm.value.title,
        description: this.videoForm.value.description || '',
        courseId: this.courseId,
        videoUrl: this.videoForm.value.videoUrl,
        position: this.videoForm.value.position ?? undefined,
        driveNotesFileLink: this.videoForm.value.driveNotesFileLink || undefined,
        driveNotesFileName: this.videoForm.value.driveNotesFileName || undefined,
        driveCodeFileLinks: validCodeFileEntries.map(entry => entry.link),
        driveCodeFileNames: validCodeFileEntries.map(entry => entry.name)
      };

      // Debug logging (can be removed in production)
      console.log('Submitting video with code files:', validCodeFileEntries.length);

      if (this.editingVideo) {
        // Update existing video
        const video = this.editingVideo as Video;
        this.videoService.updateVideo(video.id, videoData).subscribe({
          next: (response) => {
            this.saving = false;
            if (response.success) {
              this.showSuccess('Video updated successfully');
              this.loadVideos();
              this.cancelForm();
            } else {
              this.showError('Failed to update video');
            }
          },
          error: (error) => {
            this.saving = false;
            console.error('Error updating video:', error);
            this.showError('Error updating video');
          }
        });
      } else {
        // Create new video
        this.videoService.createVideo(videoData).subscribe({
          next: (response) => {
            this.saving = false;
            if (response.success) {
              this.showSuccess('Video created successfully');
              this.loadVideos();
              this.cancelForm();
            } else {
              this.showError('Failed to create video');
            }
          },
          error: (error) => {
            this.saving = false;
            console.error('Error creating video:', error);
            this.showError('Error creating video');
          }
        });
      }
    }
  }

  // Drag & drop reordering
  drop(event: CdkDragDrop<Video[]>) {
    if (!this.videos || this.videos.length < 2) return;
    if (event.previousIndex === event.currentIndex) return; // no-op

    // Local move for immediate UX
    moveItemInArray(this.videos, event.previousIndex, event.currentIndex);

    // Compute new positions locally for immediate feedback
    this.videos.forEach((v, idx) => (v.position = idx + 1));

    // Persist only the moved item's position; server shifts others
    const moved = this.videos[event.currentIndex];
    const newPos = event.currentIndex + 1;
    firstValueFrom(this.videoService.updatePosition(moved.id, newPos))
      .then(() => this.showSuccess('Order updated'))
      .catch(() => this.showError('Failed to update order'));
  }

  deleteVideo(video: Video): void {
    if (confirm(`Are you sure you want to delete the video "${video.title}"?`)) {
      this.videoService.deleteVideo(video.id).subscribe({
        next: (response) => {
          if (response.success) {
            this.showSuccess('Video deleted successfully');
            this.loadVideos();
          } else {
            this.showError('Failed to delete video');
          }
        },
        error: (error) => {
          console.error('Error deleting video:', error);
          this.showError('Error deleting video');
        }
      });
    }
  }

  getYouTubeEmbedUrl(videoUrl: string): SafeResourceUrl {
    const embedUrl = this.videoService.getYouTubeEmbedUrl(videoUrl) || videoUrl;
    return this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
  }

  // Build YouTube thumbnail URL (hqdefault)
  getYouTubeThumbnailUrl(videoUrl: string): string | null {
    const id = this.videoService.getYouTubeVideoId(videoUrl);
    return id ? `https://img.youtube.com/vi/${id}/hqdefault.jpg` : null;
  }

  goBack(): void {
    this.router.navigate(['/admin']);
  }

  // Google Drive file handling methods
  addCodeFile(): void {
    this.codeFileLinks.push('');
    this.codeFileNames.push('');
    this.updateFormArrays();
  }

  removeCodeFile(index: number): void {
    this.codeFileLinks.splice(index, 1);
    this.codeFileNames.splice(index, 1);
    this.updateFormArrays();
  }

  updateFormArrays(): void {
    // Mark form as dirty and touched to enable the submit button
    this.videoForm.markAsDirty();
    this.videoForm.markAsTouched();

    // Force change detection
    this.videoForm.updateValueAndValidity();
  }

  openGoogleDriveFile(driveUrl: string): void {
    this.videoService.openGoogleDriveFile(driveUrl);
  }

  hasFiles(video: Video): boolean {
    return !!(video.driveNotesFileLink || (video.driveCodeFileLinks && video.driveCodeFileLinks.length > 0));
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
