package com.student.studentcoursemanagement.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_video_completions")
@CompoundIndex(def = "{'userId': 1, 'videoId': 1}", unique = true)
@CompoundIndex(def = "{'userId': 1, 'courseId': 1}")
public class UserVideoCompletion {

    @Id
    private String id;

    private String userId;
    private String courseId;
    private String videoId;

    @Builder.Default
    private LocalDateTime completedAt = LocalDateTime.now();
}
