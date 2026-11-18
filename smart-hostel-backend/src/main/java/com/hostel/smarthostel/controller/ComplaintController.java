package com.hostel.smarthostel.controller;

import com.hostel.smarthostel.model.*;
import com.hostel.smarthostel.repository.NotificationRepository;
import com.hostel.smarthostel.service.AuthService;
import com.hostel.smarthostel.service.ComplaintService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ComplaintController {

    /** strategy is optional: CATEGORY (default) or KEYWORD. */
    public record SubmitRequest(ComplaintCategory category, String description, String strategy) { }
    public record AssignRequest(Long staffId) { }
    public record NoteRequest(String note) { }

    private final ComplaintService complaints;
    private final AuthService auth;
    private final NotificationRepository notifications;

    public ComplaintController(ComplaintService complaints, AuthService auth, NotificationRepository notifications) {
        this.complaints = complaints;
        this.auth = auth;
        this.notifications = notifications;
    }

    @PostMapping("/complaints")
    public Complaint submit(@RequestHeader("X-User-Id") Long userId, @RequestBody SubmitRequest request) {
        Student student = auth.require(userId, Student.class);
        return complaints.submit(student, request.category(), request.description(), request.strategy());
    }

    @GetMapping("/complaints/my")
    public List<Complaint> mine(@RequestHeader("X-User-Id") Long userId) {
        Student student = auth.require(userId, Student.class);
        return complaints.forStudent(student.getId());
    }

    @GetMapping("/complaints")
    public List<Complaint> all(@RequestHeader("X-User-Id") Long userId) {
        auth.require(userId, Warden.class);
        return complaints.all();
    }

    @GetMapping("/complaints/assigned")
    public List<Complaint> assigned(@RequestHeader("X-User-Id") Long userId) {
        MaintenanceStaff staff = auth.require(userId, MaintenanceStaff.class);
        return complaints.assignedTo(staff.getId());
    }

    @GetMapping("/complaints/{id}")
    public Complaint one(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        auth.requireAny(userId);
        return complaints.get(id);
    }

    @PutMapping("/complaints/{id}/assign")
    public Complaint assign(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id,
                            @RequestBody AssignRequest request) {
        auth.require(userId, Warden.class);
        return complaints.assign(id, request.staffId());
    }

    @PutMapping("/complaints/{id}/start")
    public Complaint start(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id,
                           @RequestBody NoteRequest request) {
        MaintenanceStaff staff = auth.require(userId, MaintenanceStaff.class);
        return complaints.startWork(id, staff, request.note());
    }

    @PutMapping("/complaints/{id}/resolve")
    public Complaint resolve(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id,
                             @RequestBody NoteRequest request) {
        MaintenanceStaff staff = auth.require(userId, MaintenanceStaff.class);
        return complaints.resolve(id, staff, request.note());
    }

    /** Notifications created by the Observer pattern for the calling user. */
    @GetMapping("/notifications")
    public List<Notification> myNotifications(@RequestHeader("X-User-Id") Long userId) {
        User user = auth.requireAny(userId);
        return notifications.findByRecipientIdOrderByIdDesc(user.getId());
    }
}
