package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.CourseRequestDTO;
import com.student.studentcoursemanagement.dto.CourseResponseDTO;
import com.student.studentcoursemanagement.exception.CourseNotFoundException;
import com.student.studentcoursemanagement.model.Course;
import com.student.studentcoursemanagement.model.CourseCategory;
import com.student.studentcoursemanagement.model.DifficultyLevel;
import com.student.studentcoursemanagement.repo.CourseRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepo courseRepository;

    @InjectMocks
    private CourseService courseService;

    private CourseRequestDTO courseRequestDTO;
    private Course course;

    @BeforeEach
    void setUp() {
        courseRequestDTO = CourseRequestDTO.builder()
                .title("Java Basics")
                .description("Learn the fundamentals of Java programming")
                .category(CourseCategory.JAVA)
                .difficulty(DifficultyLevel.BEGINNER)
                .thumbnailUrl("https://example.com/java-basics.jpg")
                .build();

        course = Course.builder()
                .id("1")
                .title("Java Basics")
                .description("Learn the fundamentals of Java programming")
                .category(CourseCategory.JAVA)
                .difficulty(DifficultyLevel.BEGINNER)
                .thumbnailUrl("https://example.com/java-basics.jpg")
                .createdBy("user123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateCourse_Success() {
        // Given
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // When
        ApiResponse<CourseResponseDTO> response = courseService.createCourse(courseRequestDTO, "user123");

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Course created successfully", response.getMessage());
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getData());
        assertEquals("Java Basics", response.getData().getTitle());
        
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testGetCourseById_Success() {
        // Given
        when(courseRepository.findById("1")).thenReturn(Optional.of(course));

        // When
        ApiResponse<CourseResponseDTO> response = courseService.getCourseById("1");

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Course retrieved successfully", response.getMessage());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getData());
        assertEquals("1", response.getData().getId());
        assertEquals("Java Basics", response.getData().getTitle());
        
        verify(courseRepository, times(1)).findById("1");
    }

    @Test
    void testGetCourseById_NotFound() {
        // Given
        when(courseRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class, 
                () -> courseService.getCourseById("999")
        );
        
        assertEquals("Course not found with ID: 999", exception.getMessage());
        verify(courseRepository, times(1)).findById("999");
    }

    @Test
    void testGetAllCourses_NoFilters() {
        // Given
        List<Course> courses = Arrays.asList(course);
        when(courseRepository.findAll()).thenReturn(courses);

        // When
        ApiResponse<List<CourseResponseDTO>> response = courseService.getAllCourses(null, null, null);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Courses retrieved successfully", response.getMessage());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("Java Basics", response.getData().get(0).getTitle());
        
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    void testDeleteCourse_Success() {
        // Given
        when(courseRepository.findById("1")).thenReturn(Optional.of(course));
        doNothing().when(courseRepository).deleteById("1");

        // When
        ApiResponse<String> response = courseService.deleteCourse("1");

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Course deleted successfully", response.getMessage());
        assertEquals(200, response.getStatusCode());
        
        verify(courseRepository, times(1)).findById("1");
        verify(courseRepository, times(1)).deleteById("1");
    }
}
