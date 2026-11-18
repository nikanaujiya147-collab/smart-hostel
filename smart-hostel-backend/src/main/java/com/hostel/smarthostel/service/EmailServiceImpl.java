package com.hostel.smarthostel.service;

import com.hostel.smarthostel.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username:}") private String mailUsername;
    @Value("${app.mail.from:}") private String mailFrom;

    public EmailServiceImpl(JavaMailSender mailSender) { this.mailSender = mailSender; }

    @Override
    public void sendWelcomeEmail(User user) {
        if (mailUsername == null || mailUsername.isBlank()) {
            log.info("Email not configured");
            return;
        }
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(mailFrom == null || mailFrom.isBlank() ? mailUsername : mailFrom);
            helper.setSubject("Welcome to Sunrise Hostel");
            String name = escape(user.getFullName());
            String username = escape(user.getUsername());
            helper.setText("<html><body style='font-family:Arial,sans-serif;color:#243247'>" +
                    "<h2>Welcome to Sunrise Hostel, " + name + "!</h2>" +
                    "<p>Your student account has been created successfully.</p><p><b>Username:</b> " + username + "</p>" +
                    "<p>You can now use the portal to:</p><ul><li>View room details</li><li>Submit and track complaints</li>" +
                    "<li>View fees and receipts</li><li>Read hostel notices</li><li>Check mess menus and preferences</li></ul>" +
                    "<p>Regards,<br>Sunrise Hostel Team</p></body></html>", true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Welcome email could not be sent for user {}", user.getUsername(), ex);
        }
    }
    private String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
