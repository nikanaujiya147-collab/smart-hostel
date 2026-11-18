package com.hostel.smarthostel.controller;

import com.hostel.smarthostel.model.Student;
import com.hostel.smarthostel.model.Visitor;
import com.hostel.smarthostel.model.Warden;
import com.hostel.smarthostel.service.AuthService;
import com.hostel.smarthostel.service.VisitorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    public record VisitorRequest(Long studentId, String visitorName, String visitorPhone, String purpose) { }

    private final VisitorService visitors;
    private final AuthService auth;

    public VisitorController(VisitorService visitors, AuthService auth) {
        this.visitors = visitors;
        this.auth = auth;
    }

    @PostMapping
    public Visitor record(@RequestHeader("X-User-Id") Long userId, @RequestBody VisitorRequest request) {
        auth.require(userId, Warden.class);
        return visitors.record(request.studentId(), request.visitorName(), request.visitorPhone(), request.purpose());
    }

    @PutMapping("/{id}/checkout")
    public Visitor checkOut(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        auth.require(userId, Warden.class);
        return visitors.checkOut(id);
    }

    @PutMapping("/{id}/verify")
    public Visitor verify(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        Warden warden = auth.require(userId, Warden.class);
        return visitors.verify(id, warden);
    }

    @GetMapping
    public List<Visitor> all(@RequestHeader("X-User-Id") Long userId) {
        auth.require(userId, Warden.class);
        return visitors.all();
    }

    @GetMapping("/my")
    public List<Visitor> mine(@RequestHeader("X-User-Id") Long userId) {
        Student student = auth.require(userId, Student.class);
        return visitors.forStudent(student.getId());
    }
}
