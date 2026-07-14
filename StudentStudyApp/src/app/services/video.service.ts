import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Video, VideoDto, ApiResponse } from '../models/video.model';

@Injectable({
  providedIn: 'root'
})
export class VideoService {
  // private baseUrl = 'https://studiehub-backend-latest.onrender.com/api/videos';

  private baseUrl = 'http://localhost:8080/api/videos';
  constructor(private http: HttpClient) { }

  /**
   * Create a new video (YouTube URL only)
   */
  createVideo(video: VideoDto): Observable<ApiResponse<Video>> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    return this.http.post<ApiResponse<Video>>(this.baseUrl, video, { headers });
  }

  /**
   * Get video by ID
   */
  getVideoById(id: string): Observable<ApiResponse<Video>> {
    return this.http.get<ApiResponse<Video>>(`${this.baseUrl}/${id}`);
  }

  /**
   * Get all videos for a course
   */
  getVideosByCourseId(courseId: string): Observable<ApiResponse<Video[]>> {
    return this.http.get<ApiResponse<Video[]>>(`${this.baseUrl}/course/${courseId}`);
  }

  /**
   * Update video details
   */
  updateVideo(id: string, video: VideoDto): Observable<ApiResponse<Video>> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    return this.http.put<ApiResponse<Video>>(`${this.baseUrl}/${id}`, video, { headers });
  }

  /**
   * Delete video
   */
  deleteVideo(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }



  /** Update only the position of a video */
  updatePosition(id: string, position: number): Observable<ApiResponse<Video>> {
    const params = new URLSearchParams({ position: String(position) });
    return this.http.patch<ApiResponse<Video>>(`${this.baseUrl}/${id}/position?${params.toString()}`, {});
  }

  /**
   * Open Google Drive file in new tab
   */
  openGoogleDriveFile(driveUrl: string): void {
    if (driveUrl) {
      window.open(driveUrl, '_blank');
    }
  }

  /**
   * Validate YouTube URL
   */
  isValidYouTubeUrl(url: string): boolean {
    if (!url) return false;
    const youtubePattern = /^https?:\/\/(www\.)?(youtube\.com\/watch\?v=|youtu\.be\/)[a-zA-Z0-9_-]{11}.*$/;
    return youtubePattern.test(url);
  }

  /**
   * Extract YouTube video ID from URL
   */
  getYouTubeVideoId(url: string): string | null {
    if (!url) return null;

    const patterns = [
      /(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/,
      /youtube\.com\/embed\/([a-zA-Z0-9_-]{11})/
    ];

    for (const pattern of patterns) {
      const match = url.match(pattern);
      if (match) return match[1];
    }

    return null;
  }

  /**
   * Convert YouTube URL to embed URL
   */
  getYouTubeEmbedUrl(url: string): string | null {
    const videoId = this.getYouTubeVideoId(url);
    return videoId ? `https://www.youtube.com/embed/${videoId}` : null;
  }
}
