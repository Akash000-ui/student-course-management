package com.student.studentcoursemanagement.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Builder
@Data
@Document(collection = "videos")
public class Video {

    @Id
    private String id;

    private String title;

    private String courseId;

    private String description;

    private String url;

    private String notes;

    private List<String> resources;
}
