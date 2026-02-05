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

        // Get all completion records for this user+course
        List<String> allCompletedIds = completionRepo.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(UserVideoCompletion::getVideoId)
                .collect(Collectors.toList());

        // ✅ FILTER: Only count videos that still exist (ignore deleted videos)
        List<String> validCompletedIds = allCompletedIds.stream()
                .filter(id -> videoRepo.existsById(id))
                .collect(Collectors.toList());

        long totalCompleted = validCompletedIds.size();
        long totalVideos = videoRepo.countByCourseId(courseId);

        return new VideoCompletionResponse(courseId, videoId, exists, totalCompleted, totalVideos, validCompletedIds);
    }

    public VideoCompletionResponse getProgress(String userId, String courseId) {
        // Get all completion records for this user+course
        List<String> allCompletedIds = completionRepo.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(UserVideoCompletion::getVideoId)
                .collect(Collectors.toList());

        // ✅ FILTER: Only count videos that still exist (ignore deleted videos)
        List<String> validCompletedIds = allCompletedIds.stream()
                .filter(id -> videoRepo.existsById(id))
                .collect(Collectors.toList());

        long totalCompleted = validCompletedIds.size();
        long totalVideos = videoRepo.countByCourseId(courseId);

        return new VideoCompletionResponse(courseId, null, false, totalCompleted, totalVideos, validCompletedIds);
    }

    public void deleteCompletionsByVideoIdIfExists(String id) {
        List<UserVideoCompletion> userVideoCompletions = completionRepo.getUserVideoCompletionByVideoId(id);

        if (userVideoCompletions.isEmpty()){
            return;
        } else {
            completionRepo.deleteByVideoId(id);
        }
    }
}
