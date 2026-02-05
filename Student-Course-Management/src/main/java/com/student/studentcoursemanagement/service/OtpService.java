package com.student.studentcoursemanagement.service;

import com.student.studentcoursemanagement.model.EmailOtp;
import com.student.studentcoursemanagement.repo.EmailOtpRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private EmailOtpRepo otpRepo;

    @Autowired
    private EmailService emailService;

    /**
     * Generate and send OTP to email
     */
    @Transactional
    public void generateAndSendOtp(String email) {
        // Delete any existing OTP for this email
        otpRepo.deleteByEmail(email);

        // Generate 6-digit OTP
        String otp = generateOtp();

        // Create and save OTP record
        EmailOtp emailOtp = EmailOtp.builder()
                .email(email)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                .verified(false)
                .attemptCount(0)
                .build();

        otpRepo.save(emailOtp);

        // Send OTP email
        emailService.sendOtpEmail(email, otp);

        logger.info("OTP generated and sent to email: {}", email);
    }

    /**
     * Verify OTP for email
     */
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        Optional<EmailOtp> otpOptional = otpRepo.findByEmail(email);

        if (otpOptional.isEmpty()) {
            logger.warn("No OTP found for email: {}", email);
            return false;
        }

        EmailOtp emailOtp = otpOptional.get();

        // Check if already verified
        if (emailOtp.isVerified()) {
            logger.warn("OTP already verified for email: {}", email);
            return true;
        }

        // Check if expired
        if (LocalDateTime.now().isAfter(emailOtp.getExpiresAt())) {
            logger.warn("OTP expired for email: {}", email);
            otpRepo.deleteByEmail(email);
            return false;
        }

        // Check attempt count
        if (emailOtp.getAttemptCount() >= MAX_ATTEMPTS) {
            logger.warn("Max OTP attempts exceeded for email: {}", email);
            otpRepo.deleteByEmail(email);
            return false;
        }

        // Increment attempt count
        emailOtp.setAttemptCount(emailOtp.getAttemptCount() + 1);
        otpRepo.save(emailOtp);

        // Verify OTP
        if (emailOtp.getOtp().equals(otp)) {
            emailOtp.setVerified(true);
            otpRepo.save(emailOtp);
            logger.info("OTP verified successfully for email: {}", email);
            return true;
        }

        logger.warn("Invalid OTP provided for email: {}", email);
        return false;
    }

    /**
     * Check if email has been verified
     */
    public boolean isEmailVerified(String email) {
        Optional<EmailOtp> otpOptional = otpRepo.findByEmail(email);
        return otpOptional.isPresent() && otpOptional.get().isVerified();
    }

    /**
     * Generate 6-digit OTP
     */
    private String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Delete OTP record after successful verification
     */
    @Transactional
    public void deleteOtp(String email) {
        otpRepo.deleteByEmail(email);
        logger.info("OTP deleted for email: {}", email);
    }
}
