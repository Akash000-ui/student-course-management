export interface VideoCompletionResponse {
    courseId: string;
    videoId?: string | null;
    alreadyCompleted: boolean;
    totalCompleted: number;
    totalVideos: number;
    completedVideoIds: string[];
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    statusCode?: number;
}
