package com.student.studentcoursemanagement.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.student.studentcoursemanagement.model.Course;
import com.student.studentcoursemanagement.model.User;
import com.student.studentcoursemanagement.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private UserRepo userRepo;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name:StudieHub}")
    private String fromName;

    @Value("${email.notification.enabled:true}")
    private boolean notificationsEnabled;

    @Value("${email.notification.batch.size:100}")
    private int batchSize;

    @Value("${email.notification.batch.delay.ms:5000}")
    private long batchDelayMs;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            String subject = "StudieHub - Email Verification OTP";
            Content content = new Content("text/plain", buildOtpEmailBody(otp));
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("OTP email sent successfully to: {} (Status: {})", toEmail, response.getStatusCode());
            } else {
                logger.error("Failed to send OTP email. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send OTP email. Status: " + response.getStatusCode());
            }
        } catch (IOException e) {
            logger.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    private String buildOtpEmailBody(String otp) {
        return String.format(
                "Welcome to StudieHub!\n\n" +
                        "Your email verification OTP is: %s\n\n" +
                        "This OTP is valid for 10 minutes.\n\n" +
                        "If you didn't request this verification, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "StudieHub Team",
                otp);
    }

    /**
     * Send course notification to all users asynchronously in batches
     * 
     * @param course The course that was created or updated
     * @param action "CREATED" or "UPDATED"
     */
    @Async("emailTaskExecutor")
    public void sendCourseNotificationToAllUsers(Course course, String action) {
        // Check if notifications are enabled
        if (!notificationsEnabled) {
            logger.info("Email notifications are disabled. Skipping notification for course: {}", course.getTitle());
            return;
        }

        logger.info("Starting to send course {} notification for: {}", action, course.getTitle());

        try {
            // Fetch all users
            List<User> allUsers = userRepo.findAll();
            logger.info("Found {} users to notify", allUsers.size());

            if (allUsers.isEmpty()) {
                logger.warn("No users found to send notifications");
                return;
            }

            int totalUsers = allUsers.size();
            int sentCount = 0;
            int failedCount = 0;

            // Process users in batches
            for (int i = 0; i < totalUsers; i += batchSize) {
                int endIndex = Math.min(i + batchSize, totalUsers);
                List<User> batch = allUsers.subList(i, endIndex);

                logger.info("Processing batch {}/{} (users {}-{})",
                        (i / batchSize) + 1,
                        (int) Math.ceil((double) totalUsers / batchSize),
                        i + 1,
                        endIndex);

                // Send emails to current batch
                for (User user : batch) {
                    try {
                        sendCourseNotificationEmail(user, course, action);
                        sentCount++;
                    } catch (Exception e) {
                        failedCount++;
                        logger.error("Failed to send email to user: {} - {}", user.getEmail(), e.getMessage());
                    }
                }

                // Add delay between batches to avoid rate limiting (except for last batch)
                if (endIndex < totalUsers) {
                    try {
                        Thread.sleep(batchDelayMs);
                        logger.info("Waiting {} seconds before next batch...", batchDelayMs / 1000);
                    } catch (InterruptedException e) {
                        logger.error("Batch delay interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                }
            }

            logger.info("Course notification completed. Sent: {}, Failed: {}, Total: {}",
                    sentCount, failedCount, totalUsers);

        } catch (Exception e) {
            logger.error("Error sending course notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Send individual course notification email to a user
     */
    private void sendCourseNotificationEmail(User user, Course course, String action) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(user.getEmail());
            String subject;
            String body;

            if ("CREATED".equalsIgnoreCase(action)) {
                subject = "New Course Available: " + course.getTitle();
                body = buildNewCourseEmailBody(user, course);
            } else if ("UPDATED".equalsIgnoreCase(action)) {
                subject = "Course Updated: " + course.getTitle();
                body = buildCourseUpdateEmailBody(user, course);
            } else {
                return;
            }

            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.debug("Notification email sent to: {}", user.getEmail());
            } else {
                throw new IOException("SendGrid returned status: " + response.getStatusCode());
            }
        } catch (IOException e) {
            logger.error("Failed to send notification to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send notification: " + e.getMessage());
        }
    }

    /**
     * Build email body for new course notification
     */
    private String buildNewCourseEmailBody(User user, Course course) {
        return String.format(
                "Hello %s,\n\n" +
                        "Exciting news! A new course has been added to StudieHub:\n\n" +
                        "📚 Course: %s\n" +
                        "👨‍🏫 Trainer: %s\n" +
                        "📊 Difficulty: %s\n" +
                        "🌐 Language: %s\n\n" +
                        "Description:\n%s\n\n" +
                        "Don't miss out on this opportunity to expand your knowledge!\n\n" +
                        "Login to StudieHub to explore this course and start learning today.\n\n" +
                        "Happy Learning!\n" +
                        "The StudieHub Team",
                user.getUsername(),
                course.getTitle(),
                course.getTrainerName(),
                course.getDifficulty(),
                course.getLanguage(),
                course.getDescription().length() > 200
                        ? course.getDescription().substring(0, 200) + "..."
                        : course.getDescription());
    }

    /**
     * Build email body for course update notification
     */
    private String buildCourseUpdateEmailBody(User user, Course course) {
        return String.format(
                "Hello %s,\n\n" +
                        "A course has been updated on StudieHub:\n\n" +
                        "📚 Course: %s\n" +
                        "👨‍🏫 Trainer: %s\n" +
                        "📊 Difficulty: %s\n" +
                        "🌐 Language: %s\n\n" +
                        "Updated Description:\n%s\n\n" +
                        "Check out the latest changes and continue your learning journey!\n\n" +
                        "Login to StudieHub to see what's new.\n\n" +
                        "Happy Learning!\n" +
                        "The StudieHub Team",
                user.getUsername(),
                course.getTitle(),
                course.getTrainerName(),
                course.getDifficulty(),
                course.getLanguage(),
                course.getDescription().length() > 200
                        ? course.getDescription().substring(0, 200) + "..."
                        : course.getDescription());
    }
}
