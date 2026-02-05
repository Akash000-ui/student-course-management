import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CourseStats {
  courseId: string;
  courseTitle: string;
  totalEnrollments: number;
  activeEnrollments: number;
  completedEnrollments: number;
  totalVideos: number;
}

export interface RecentActivity {
  newUsersToday: number;
  newEnrollmentsToday: number;
  activeUsersToday: number;
  mostPopularCourse: string;
  mostPopularCourseEnrollments: number;
}

export interface AdminDashboardStats {
  totalUsers: number;
  totalCourses: number;
  totalEnrollments: number;
  totalVideos: number;
  newUsersThisMonth: number;
  newEnrollmentsThisMonth: number;
  courseStats: CourseStats[];
  recentActivity: RecentActivity;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  statusCode: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  // private baseUrl = 'https://studiehub-backend-latest.onrender.com/api/admin';
  private baseUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) { }

  getAdminStats(): Observable<ApiResponse<AdminDashboardStats>> {
    return this.http.get<ApiResponse<AdminDashboardStats>>(`${this.baseUrl}/stats`);
  }
}
