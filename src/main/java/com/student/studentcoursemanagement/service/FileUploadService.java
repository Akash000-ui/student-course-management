//package com.student.studentcoursemanagement.service;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//@Service
//public class FileUploadService {
//
//    @Value("${file.upload.dir:uploads}")
//    private String uploadDir;
//
//    public String saveFile(MultipartFile file, String subDirectory) throws IOException {
//        if (file.isEmpty()) {
//            throw new IllegalArgumentException("File is empty");
//        }
//
//        // Create upload directory if it doesn't exist
//        String fullUploadPath = uploadDir + File.separator + subDirectory;
//        Path uploadPath = Paths.get(fullUploadPath);
//        if (!Files.exists(uploadPath)) {
//            Files.createDirectories(uploadPath);
//        }
//
//        // Generate unique filename to avoid conflicts
//        String originalFilename = file.getOriginalFilename();
//        String fileExtension = "";
//        if (originalFilename != null && originalFilename.contains(".")) {
//            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
//        }
//
//        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
//        Path filePath = uploadPath.resolve(uniqueFilename);
//
//        // Save file
//        Files.copy(file.getInputStream(), filePath);
//
//        // Return relative path for storage in database
//        return subDirectory + File.separator + uniqueFilename;
//    }
//
//    public List<String> saveMultipleFiles(List<MultipartFile> files, String subDirectory) throws IOException {
//        List<String> filePaths = new ArrayList<>();
//
//        for (MultipartFile file : files) {
//            if (!file.isEmpty()) {
//                String filePath = saveFile(file, subDirectory);
//                filePaths.add(filePath);
//            }
//        }
//
//        return filePaths;
//    }
//
//    public boolean deleteFile(String filePath) {
//        try {
//            Path path = Paths.get(uploadDir, filePath);
//            return Files.deleteIfExists(path);
//        } catch (IOException e) {
//            return false;
//        }
//    }
//
//    public void deleteMultipleFiles(List<String> filePaths) {
//        if (filePaths != null) {
//            filePaths.forEach(this::deleteFile);
//        }
//    }
//
//    public File getFile(String filePath) {
//        Path path = Paths.get(uploadDir, filePath);
//        File file = path.toFile();
//        return file.exists() ? file : null;
//    }
//
//    public String getFileUrl(String filePath) {
//        // This will be used to generate download URLs
//        return "/api/files/download/" + filePath.replace(File.separator, "/");
//    }
//}
