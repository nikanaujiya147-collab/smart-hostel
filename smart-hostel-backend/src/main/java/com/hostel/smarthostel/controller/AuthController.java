package com.hostel.smarthostel.controller;

import com.hostel.smarthostel.model.Student;
import com.hostel.smarthostel.exception.BusinessRuleException;
import com.hostel.smarthostel.service.EmailService;
import com.hostel.smarthostel.model.MaintenanceStaff;
import com.hostel.smarthostel.model.User;
import com.hostel.smarthostel.model.Warden;
import com.hostel.smarthostel.repository.UserRepository;
import com.hostel.smarthostel.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    public record LoginRequest(String username, String password) { }
    public record RegisterRequest(String username, String password, String fullName, String email,
                                  String phone, String rollNumber, String course) { }

    private final AuthService auth;
    private final UserRepository users;
    private final EmailService emailService;

    public AuthController(AuthService auth, UserRepository users) {
        this(auth, users, user -> { });
    }

    @Autowired
    public AuthController(AuthService auth, UserRepository users, EmailService emailService) {
        this.auth = auth;
        this.users = users;
        this.emailService = emailService;
    }

    /** Returns userId + role. Use the userId as the X-User-Id header in the other APIs. */
    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        User user = auth.login(request.username(), request.password());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("fullName", user.getFullName());
        result.put("role", user.getRole());           // polymorphic call
        result.put("dashboard", user.getDashboardTitle());   // polymorphic call
        return result;
    }

    @PostMapping("/auth/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        if (request == null || blank(request.username()) || blank(request.password()) || blank(request.fullName())
                || blank(request.email()) || blank(request.phone()) || blank(request.rollNumber()) || blank(request.course())) {
            throw new IllegalArgumentException("All registration fields are required");
        }
        if (!request.email().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Please provide a valid email address");
        }
        if (users.findByUsername(request.username().trim()).isPresent()) {
            throw new BusinessRuleException("Username is already taken");
        }
        Student student = new Student(request.username().trim(), request.password(), request.fullName().trim(),
                request.phone().trim(), request.rollNumber().trim(), request.course().trim());
        student.setEmail(request.email().trim());
        Student saved = (Student) users.save(student);
        emailService.sendWelcomeEmail(saved);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", saved.getId());
        result.put("fullName", saved.getFullName());
        result.put("email", saved.getEmail());
        result.put("role", saved.getRole());
        return result;
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    @GetMapping("/users/students")
    public List<User> students(@RequestHeader("X-User-Id") Long userId) {
        auth.require(userId, Warden.class);
        return users.findAll().stream().filter(u -> u instanceof Student).toList();
    }

    @GetMapping("/users/staff")
    public List<User> staff(@RequestHeader("X-User-Id") Long userId) {
        auth.require(userId, Warden.class);
        return users.findAll().stream().filter(u -> u instanceof MaintenanceStaff).toList();
    }
}
