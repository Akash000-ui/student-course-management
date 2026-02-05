export interface Video {
  id: string;
  title: string;
  description: string;
  courseId: string;
  videoUrl: string;
  position?: number;
  driveNotesFileLink?: string;
  driveNotesFileName?: string;
  driveCodeFileLinks?: string[];
  driveCodeFileNames?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface VideoDto {
  title: string;
  description: string;
  courseId: string;
  videoUrl: string;
  position?: number;
  driveNotesFileLink?: string;
  driveNotesFileName?: string;
  driveCodeFileLinks?: string[];
  driveCodeFileNames?: string[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  statusCode?: number;
  timestamp?: string;
}
