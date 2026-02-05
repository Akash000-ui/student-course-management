import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Course {
  id: string;
  title: string;
  description: string;
  categoryId: string; // Changed from category to categoryId
  difficulty: string;
  thumbnailUrl?: string;
  videoIds: string[];
  createdAt: string;
  updatedAt: string;

  // Trainer Information
  trainerName: string;
  trainerBio: string;
  experience: string;
  linkedinProfile?: string;
  fieldOfWork: string;
  profilePictureUrl?: string;
  language: string;

  // Additional properties for frontend display
  duration?: number;
  price?: number;
}

export interface CreateCourseRequest {
  title: string;
  description: string;
  categoryId: string; // Changed from category to categoryId
  difficulty: string;
  thumbnailUrl?: string;

  // Trainer Information
  trainerName: string;
  trainerBio: string;
  experience: string;
  linkedinProfile?: string;
  fieldOfWork: string;
  profilePictureUrl?: string;
  language: string;
}

export interface UpdateCourseRequest {
  title?: string;
  description?: string;
  categoryId?: string; // Changed from category to categoryId
  difficulty?: string;
  thumbnailUrl?: string;

  // Trainer Information
  trainerName?: string;
  trainerBio?: string;
  experience?: string;
  linkedinProfile?: string;
  fieldOfWork?: string;
  profilePictureUrl?: string;
  language?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  statusCode: number;
  timestamp?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  // private baseUrl = 'https://studiehub-backend-latest.onrender.com/api/courses';

  private baseUrl = 'http://localhost:8080/api/courses';

  constructor(private http: HttpClient) { }

  getAllCourses(filters?: {
    category?: string;
    difficulty?: string;
    search?: string;
  }): Observable<ApiResponse<Course[]>> {
    let params = new HttpParams();

    if (filters?.category && filters.category !== '') {
      params = params.set('category', filters.category);
    }
    if (filters?.difficulty && filters.difficulty !== '') {
      params = params.set('difficulty', filters.difficulty);
    }
    if (filters?.search && filters.search.trim() !== '') {
      params = params.set('search', filters.search.trim());
    }

    return this.http.get<ApiResponse<Course[]>>(this.baseUrl, { params });
  }

  getCourseById(id: string): Observable<ApiResponse<Course>> {
    return this.http.get<ApiResponse<Course>>(`${this.baseUrl}/${id}`);
  }

  // Admin methods for course management
  createCourse(courseData: CreateCourseRequest): Observable<ApiResponse<Course>> {
    return this.http.post<ApiResponse<Course>>(this.baseUrl, courseData);
  }

  updateCourse(id: string, courseData: UpdateCourseRequest): Observable<ApiResponse<Course>> {
    return this.http.put<ApiResponse<Course>>(`${this.baseUrl}/${id}`, courseData);
  }

  deleteCourse(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}
