export interface Enrollment {
  id: string;
  userId: string;
  courseId: string;
  courseTitle?: string;
  courseDescription?: string;
  courseThumbnail?: string;
  enrolledAt: string;
  lastAccessedAt: string;
  isCompleted: boolean;
  completedAt?: string;
  progressPercentage: number;
  completedVideos: number;
  totalVideos: number;
}

export interface EnrollmentRequest {
  courseId: string;
}

export interface EnrollmentStats {
  totalEnrollments: number;
  completedEnrollments: number;
  completionRate: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  statusCode?: number;
}
