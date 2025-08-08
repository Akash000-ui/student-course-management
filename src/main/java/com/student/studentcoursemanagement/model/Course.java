package com.student.studentcoursemanagement.model;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Builder
@Data
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String title;
    private String description;
    List<Video> videosIds;
}
