//package com.student.studentcoursemanagement.controller;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.student.studentcoursemanagement.service.FileUploadService;
//
//import jakarta.servlet.http.HttpServletRequest;
//
//@RestController
//@RequestMapping("/api/files")
//@CrossOrigin(origins = "*")
//public class FileController {
//
//    @Autowired
//    private FileUploadService fileUploadService;
//
//    @GetMapping("/download/**")
//    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
//        try {
//            // Extract the file path from the request URI
//            String requestURI = request.getRequestURI();
//            String filePath = requestURI.substring("/api/files/download/".length());
//
//            File file = fileUploadService.getFile(filePath);
//
//            if (file == null || !file.exists()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            Resource resource = new FileSystemResource(file);
//
//            // Determine content type
//            Path path = Paths.get(file.getAbsolutePath());
//            String contentType = Files.probeContentType(path);
//            if (contentType == null) {
//                contentType = "application/octet-stream";
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION,
//                            "attachment; filename=\"" + file.getName() + "\"")
//                    .body(resource);
//
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//    @GetMapping("/view/**")
//    public ResponseEntity<Resource> viewFile(HttpServletRequest request) {
//        try {
//            // Extract the file path from the request URI
//            String requestURI = request.getRequestURI();
//            String filePath = requestURI.substring("/api/files/view/".length());
//
//            File file = fileUploadService.getFile(filePath);
//
//            if (file == null || !file.exists()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            Resource resource = new FileSystemResource(file);
//
//            // Determine content type
//            Path path = Paths.get(file.getAbsolutePath());
//            String contentType = Files.probeContentType(path);
//            if (contentType == null) {
//                contentType = "application/octet-stream";
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION,
//                            "inline; filename=\"" + file.getName() + "\"")
//                    .body(resource);
//
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//}
