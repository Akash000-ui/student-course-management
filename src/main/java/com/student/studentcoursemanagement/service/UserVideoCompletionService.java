package com.student.studentcoursemanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.student.studentcoursemanagement.dto.VideoCompletionResponse;
import com.student.studentcoursemanagement.model.UserVideoCompletion;
import com.student.studentcoursemanagement.repo.UserVideoCompletionRepo;
import com.student.studentcoursemanagement.repo.VideoRepo;

@Service
public class UserVideoCompletionService {

    @Autowired
    private UserVideoCompletionRepo completionRepo;

    @Autowired
    private VideoRepo videoRepo;


    public VideoCompletionResponse markCompleted(String userId, String courseId, String videoId) {
        boolean exists = completionRepo.existsByUserIdAndVideoId(userId, videoId);
        if (!exists) {
            // validate course/video association
            var video = videoRepo.findById(videoId).orElse(null);
            if (video == null || !courseId.equals(video.getCourseId())) {
                throw new IllegalArgumentException("Video does not belong to course");
            }
            completionRepo.save(UserVideoCompletion.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .videoId(videoId)
                    .build());
        }

        long totalCompleted = completionRepo.countByUserIdAndCourseId(userId, courseId);
        long totalVideos = videoRepo.countByCourseId(courseId);
        List<String> completedIds = completionRepo.findByUserIdAndCourseId(userId, courseId)
                .stream().map(UserVideoCompletion::getVideoId).collect(Collectors.toList());

        return new VideoCompletionResponse(courseId, videoId, exists, totalCompleted, totalVideos, completedIds);
    }

    public VideoCompletionResponse getProgress(String userId, String courseId) {
        long totalCompleted = completionRepo.countByUserIdAndCourseId(userId, courseId);
        long totalVideos = videoRepo.countByCourseId(courseId);
        List<String> completedIds = completionRepo.findByUserIdAndCourseId(userId, courseId)
                .stream().map(UserVideoCompletion::getVideoId).collect(Collectors.toList());
        return new VideoCompletionResponse(courseId, null, false, totalCompleted, totalVideos, completedIds);
    }
}
