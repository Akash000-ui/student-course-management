import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, VideoCompletionResponse } from '../models/progress.model';

@Injectable({ providedIn: 'root' })
export class ProgressService {
    // private baseUrl = 'https://studiehub-backend-latest.onrender.com/api/progress';
    private baseUrl = 'http://localhost:8080/api/progress';

    constructor(private http: HttpClient) { }

    getCourseProgress(courseId: string): Observable<ApiResponse<VideoCompletionResponse>> {
        return this.http.get<ApiResponse<VideoCompletionResponse>>(`${this.baseUrl}/courses/${courseId}`);
    }

    markVideoComplete(courseId: string, videoId: string): Observable<ApiResponse<VideoCompletionResponse>> {
        return this.http.put<ApiResponse<VideoCompletionResponse>>(
            `${this.baseUrl}/courses/${courseId}/videos/${videoId}/complete`,
            {}
        );
    }
}
