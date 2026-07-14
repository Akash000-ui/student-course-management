package com.student.studentcoursemanagement.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoCompletionResponse {
    private String courseId;
    private String videoId;
    private boolean alreadyCompleted;
    private long totalCompleted;
    private long totalVideos;
    private List<String> completedVideoIds;
}
