package com.hostel.smarthostel;

import com.hostel.smarthostel.controller.AuthController;
import com.hostel.smarthostel.exception.BusinessRuleException;
import com.hostel.smarthostel.model.Student;
import com.hostel.smarthostel.repository.UserRepository;
import com.hostel.smarthostel.service.AuthService;
import com.hostel.smarthostel.service.EmailService;
import com.hostel.smarthostel.service.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;

class RegistrationTest {
    @Test void rejectsMissingRequiredFields() {
        UserRepository users = mock(UserRepository.class);
        AuthController controller = new AuthController(mock(AuthService.class), users, mock(EmailService.class));
        assertThrows(IllegalArgumentException.class, () -> controller.register(
                new AuthController.RegisterRequest("", "pass", "Name", "name@example.com", "123", "R1", "IT")));
        verify(users, never()).save(any());
    }
    @Test void rejectsDuplicateUsername() {
        UserRepository users = mock(UserRepository.class);
        when(users.findByUsername("taken")).thenReturn(Optional.of(mock(Student.class)));
        AuthController controller = new AuthController(mock(AuthService.class), users, mock(EmailService.class));
        assertThrows(BusinessRuleException.class, () -> controller.register(
                new AuthController.RegisterRequest("taken", "pass", "Name", "name@example.com", "123", "R1", "IT")));
    }
    @Test void smtpFailureDoesNotEscapeWelcomeEmailService() {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenThrow(new RuntimeException("SMTP unavailable"));
        EmailServiceImpl service = new EmailServiceImpl(sender);
        ReflectionTestUtils.setField(service, "mailUsername", "hostel@example.com");
        ReflectionTestUtils.setField(service, "mailFrom", "hostel@example.com");
        assertDoesNotThrow(() -> service.sendWelcomeEmail(new Student("newstudent", "pass", "New Student", "123", "R2", "IT")));
    }
}
