package com.student.studentcoursemanagement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.student.studentcoursemanagement.model.Course;
import com.student.studentcoursemanagement.repo.UserVideoCompletionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.student.studentcoursemanagement.dto.ApiResponse;
import com.student.studentcoursemanagement.dto.VideoRequestDTO;
import com.student.studentcoursemanagement.dto.VideoResponseDTO;
import com.student.studentcoursemanagement.exception.CourseNotFoundException;
import com.student.studentcoursemanagement.exception.InvalidVideoDataException;
import com.student.studentcoursemanagement.model.Video;
import com.student.studentcoursemanagement.repo.CourseRepo;
import com.student.studentcoursemanagement.repo.VideoRepo;

@Service
public class VideoService {

    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);

    // YouTube URL validation pattern
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "^https?://(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[a-zA-Z0-9_-]{11}.*$");

    @Autowired
    private VideoRepo videoRepository;

    @Autowired
    private CourseRepo courseRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserVideoCompletionService userVideoCompletionService;

    /**
     * Create a new video
     */
    public ApiResponse<VideoResponseDTO> createVideo(VideoRequestDTO request) {
        try {
            logger.info("Creating new video with title: {} for course: {}", request.getTitle(), request.getCourseId());

            // Validate input
            validateVideoRequest(request);

            // Validate course exists
            if (!courseRepository.existsById(request.getCourseId())) {
                logger.error("Course not found with ID: {}", request.getCourseId());
                throw new CourseNotFoundException("Course not found with ID: " + request.getCourseId());
            }

            // Validate YouTube URL
            if (StringUtils.hasText(request.getVideoUrl()) && !isValidYouTubeUrl(request.getVideoUrl())) {
                logger.error("Invalid YouTube URL: {}", request.getVideoUrl());
                throw new InvalidVideoDataException("Invalid YouTube URL. Only YouTube URLs are allowed.");
            }

            // Determine position
            Integer position = request.getPosition();

            // Log Google Drive files info
            logger.info("Creating video with {} code files",
                    request.getDriveCodeFileLinks() != null ? request.getDriveCodeFileLinks().size() : 0);

            Video video = Video.builder()
                    .title(request.getTitle().trim())
                    .description(request.getDescription() != null ? request.getDescription().trim() : null)
                    .courseId(request.getCourseId())
                    .videoUrl(request.getVideoUrl())
                    .driveNotesFileLink(request.getDriveNotesFileLink())
                    .driveNotesFileName(request.getDriveNotesFileName())
                    .driveCodeFileLinks(request.getDriveCodeFileLinks() != null ? request.getDriveCodeFileLinks()
                            : new ArrayList<>())
                    .driveCodeFileNames(request.getDriveCodeFileNames() != null ? request.getDriveCodeFileNames()
                            : new ArrayList<>())
                    .position(null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(); // Assign position (shift if inserting in the middle)
            assignPositionOnCreate(video, position);
            Video savedVideo = videoRepository.save(video);

            // No need to update enrollments - progress is calculated dynamically
            //Now Course Model need to be updated for video ids

            Optional<Course> course = courseRepository.findById(request.getCourseId());

            if(!course.isPresent()){
                throw new CourseNotFoundException("Course not found with ID: " + request.getCourseId());
            }
            Course existingCourse = course.get();
            List<String> videoIds = existingCourse.getVideoIds();
            if(videoIds == null){
                videoIds = new ArrayList<>();
            }
            videoIds.add(savedVideo.getId());
            existingCourse.setVideoIds(videoIds);
            courseRepository.save(existingCourse);

            ApiResponse<VideoResponseDTO> response = new ApiResponse<>(
                    true,
                    "Video created successfully",
                    convertToResponseDTO(savedVideo),
                    201);

            logger.info("Video created successfully with ID: {}", savedVideo.getId());
            return response;

        } catch (CourseNotFoundException | InvalidVideoDataException e) {
            logger.error("Validation error while creating video: {}", e.getMessage());
            return new ApiResponse<>(false, e.getMessage(), null, 400);
        } catch (Exception e) {
            logger.error("Unexpected error while creating video", e);
            return new ApiResponse<>(false, "Failed to create video: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Get video by ID
     */
    public ApiResponse<VideoResponseDTO> getVideoById(String id) {
        try {
            logger.info("Fetching video with ID: {}", id);

            Optional<Video> videoOpt = videoRepository.findById(id);
            if (videoOpt.isEmpty()) {
                logger.warn("Video not found with ID: {}", id);
                return new ApiResponse<>(false, "Video not found with ID: " + id, null, 404);
            }

            Video video = videoOpt.get();
            VideoResponseDTO responseDTO = convertToResponseDTO(video);

            logger.info("Video fetched successfully: {}", video.getTitle());
            return new ApiResponse<>(true, "Video retrieved successfully", responseDTO, 200);

        } catch (Exception e) {
            logger.error("Error fetching video with ID: {}", id, e);
            return new ApiResponse<>(false, "Failed to retrieve video: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Get all videos for a course
     */
    public ApiResponse<List<VideoResponseDTO>> getVideosByCourseId(String courseId) {
        try {
            logger.info("Fetching videos for course ID: {}", courseId);

            // Validate course exists
            if (!courseRepository.existsById(courseId)) {
                logger.error("Course not found with ID: {}", courseId);
                return new ApiResponse<>(false, "Course not found with ID: " + courseId, null, 404);
            }

            List<Video> videos = videoRepository.findByCourseIdOrderByPositionAsc(courseId);
            // Normalize positions if missing (legacy data)
            if (videos.isEmpty() || videos.stream().anyMatch(v -> v.getPosition() == null)) {
                normalizePositions(courseId);
                videos = videoRepository.findByCourseIdOrderByPositionAsc(courseId);
            }
            List<VideoResponseDTO> responseDTOs = videos.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            logger.info("Found {} videos for course ID: {}", videos.size(), courseId);
            return new ApiResponse<>(true, "Videos retrieved successfully", responseDTOs, 200);

        } catch (Exception e) {
            logger.error("Error fetching videos for course ID: {}", courseId, e);
            return new ApiResponse<>(false, "Failed to retrieve videos: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Update video
     */
    public ApiResponse<VideoResponseDTO> updateVideo(String id, VideoRequestDTO request) {
        try {
            logger.info("Updating video with ID: {}", id);

            Optional<Video> videoOpt = videoRepository.findById(id);
            if (videoOpt.isEmpty()) {
                logger.warn("Video not found with ID: {}", id);
                return new ApiResponse<>(false, "Video not found with ID: " + id, null, 404);
            }

            Video existingVideo = videoOpt.get();

            // Validate input
            validateVideoRequest(request);

            // Validate course exists if courseId is being changed
            if (!existingVideo.getCourseId().equals(request.getCourseId()) &&
                    !courseRepository.existsById(request.getCourseId())) {
                logger.error("Course not found with ID: {}", request.getCourseId());
                return new ApiResponse<>(false, "Course not found with ID: " + request.getCourseId(), null, 400);
            }

            // Validate YouTube URL
            if (StringUtils.hasText(request.getVideoUrl()) && !isValidYouTubeUrl(request.getVideoUrl())) {
                logger.error("Invalid YouTube URL: {}", request.getVideoUrl());
                return new ApiResponse<>(false, "Invalid YouTube URL. Only YouTube URLs are allowed.", null, 400);
            }

            // Update fields
            existingVideo.setTitle(request.getTitle().trim());
            existingVideo.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            existingVideo.setCourseId(request.getCourseId());
            existingVideo.setVideoUrl(request.getVideoUrl());
            existingVideo.setDriveNotesFileLink(request.getDriveNotesFileLink());
            existingVideo.setDriveNotesFileName(request.getDriveNotesFileName());
            existingVideo.setDriveCodeFileLinks(
                    request.getDriveCodeFileLinks() != null ? request.getDriveCodeFileLinks() : new ArrayList<>());
            existingVideo.setDriveCodeFileNames(
                    request.getDriveCodeFileNames() != null ? request.getDriveCodeFileNames() : new ArrayList<>());
            existingVideo.setUpdatedAt(LocalDateTime.now());

            // Handle position change if provided
            if (request.getPosition() != null) {
                updateVideoPositionInternal(existingVideo, request.getPosition());
            }

            Video updatedVideo = videoRepository.save(existingVideo);
            VideoResponseDTO responseDTO = convertToResponseDTO(updatedVideo);

            logger.info("Video updated successfully: {}", updatedVideo.getTitle());
            return new ApiResponse<>(true, "Video updated successfully", responseDTO, 200);

        } catch (Exception e) {
            logger.error("Error updating video with ID: {}", id, e);
            return new ApiResponse<>(false, "Failed to update video: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Delete video
     */
    public ApiResponse<Void> deleteVideo(String id) {
        try {
            logger.info("Deleting video with ID: {}", id);

            Optional<Video> videoOpt = videoRepository.findById(id);
            if (videoOpt.isEmpty()) {
                logger.warn("Video not found with ID: {}", id);
                return new ApiResponse<>(false, "Video not found with ID: " + id, null, 404);
            }

            Video video = videoOpt.get();

            videoRepository.deleteById(id);

            userVideoCompletionService.deleteCompletionsByVideoIdIfExists(id);



            logger.info("Video deleted successfully: {}", video.getTitle());
            return new ApiResponse<>(true, "Video deleted successfully", null, 200);

        } catch (Exception e) {
            logger.error("Error deleting video with ID: {}", id, e);
            return new ApiResponse<>(false, "Failed to delete video: " + e.getMessage(), null, 500);
        }
    }

    /**
     * Validate video request data
     */
    private void validateVideoRequest(VideoRequestDTO request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new InvalidVideoDataException("Video title is required");
        }
        if (request.getTitle().trim().length() < 3 || request.getTitle().trim().length() > 100) {
            throw new InvalidVideoDataException("Video title must be between 3 and 100 characters");
        }
        if (!StringUtils.hasText(request.getCourseId())) {
            throw new InvalidVideoDataException("Course ID is required");
        }
        // Description is optional, but if provided, it should not be blank
        if (request.getDescription() != null && request.getDescription().trim().isEmpty()) {
            throw new InvalidVideoDataException("Description cannot be empty if provided");
        }
    }

    /**
     * Validate YouTube URL
     */
    private boolean isValidYouTubeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return YOUTUBE_PATTERN.matcher(url.trim()).matches();
    }

    /**
     * Convert Video entity to VideoResponseDTO
     */
    private VideoResponseDTO convertToResponseDTO(Video video) {
        return VideoResponseDTO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .courseId(video.getCourseId())
                .videoUrl(video.getVideoUrl())
                .driveNotesFileLink(video.getDriveNotesFileLink())
                .driveNotesFileName(video.getDriveNotesFileName())
                .driveCodeFileLinks(video.getDriveCodeFileLinks())
                .driveCodeFileNames(video.getDriveCodeFileNames())
                .position(video.getPosition())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }

    // --- Ordering helpers --

    private void assignPositionOnCreate(Video video, Integer requestedPosition) {
        final String courseId = video.getCourseId();
        // Ensure existing course videos have normalized positions
        normalizePositions(courseId);
        List<Video> existing = videoRepository.findByCourseIdOrderByPositionAsc(courseId);
        int size = existing.size();
        int newPos = (requestedPosition == null || requestedPosition < 1) ? size + 1
                : Math.min(requestedPosition, size + 1);
        // Shift videos at or after newPos
        for (Video v : existing) {
            if (v.getPosition() != null && v.getPosition() >= newPos) {
                v.setPosition(v.getPosition() + 1);
            }
        }
        if (!existing.isEmpty()) {
            videoRepository.saveAll(existing);
        }
        video.setPosition(newPos);
    }

    private void normalizePositions(String courseId) {
        List<Video> videos = videoRepository.findByCourseIdOrderByCreatedAtAsc(courseId);
        if (videos.isEmpty())
            return;
        boolean needsSave = false;
        int pos = 1;
        for (Video v : videos) {
            if (v.getPosition() == null || v.getPosition() != pos) {
                v.setPosition(pos);
                needsSave = true;
            }
            pos++;
        }
        if (needsSave) {
            videoRepository.saveAll(videos);
        }
    }

    private void updateVideoPositionInternal(Video video, int requestedPosition) {
        String courseId = video.getCourseId();
        normalizePositions(courseId);
        List<Video> videos = videoRepository.findByCourseIdOrderByPositionAsc(courseId);
        int size = videos.size();
        int oldPos = video.getPosition() == null ? size : video.getPosition();
        int newPos = requestedPosition < 1 ? 1 : Math.min(requestedPosition, size);
        if (newPos == oldPos)
            return;

        for (Video v : videos) {
            if (v.getId().equals(video.getId()))
                continue;
            Integer p = v.getPosition();
            if (p == null)
                continue;
            if (newPos < oldPos) {
                // Shift up: [newPos, oldPos-1] +1
                if (p >= newPos && p < oldPos) {
                    v.setPosition(p + 1);
                }
            } else {
                // Shift down: (oldPos, newPos] -1
                if (p > oldPos && p <= newPos) {
                    v.setPosition(p - 1);
                }
            }
        }
        videoRepository
                .saveAll(videos.stream().filter(v -> !v.getId().equals(video.getId())).collect(Collectors.toList()));
        video.setPosition(newPos);
    }

    public ApiResponse<VideoResponseDTO> updateVideoPosition(String videoId, int position) {
        try {
            Optional<Video> opt = videoRepository.findById(videoId);
            if (opt.isEmpty()) {
                return new ApiResponse<>(false, "Video not found with ID: " + videoId, null, 404);
            }
            Video video = opt.get();
            updateVideoPositionInternal(video, position);
            video.setUpdatedAt(LocalDateTime.now());
            video = videoRepository.save(video);
            return new ApiResponse<>(true, "Video position updated", convertToResponseDTO(video), 200);
        } catch (Exception e) {
            logger.error("Error updating video position {}: {}", videoId, e.getMessage());
            return new ApiResponse<>(false, "Failed to update video position: " + e.getMessage(), null, 500);
        }
    }

}