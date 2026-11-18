package com.hostel.smarthostel.controller;

import com.hostel.smarthostel.model.*;
import com.hostel.smarthostel.service.AuthService;
import com.hostel.smarthostel.service.MessService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/mess")
public class MessController {

    public record MenuRequest(LocalDate menuDate, MealType mealType, String items) { }
    public record FeedbackRequest(Long menuId, int rating, String comment) { }
    public record PreferenceRequest(MealType mealType, String preference) { }

    private final MessService mess;
    private final AuthService auth;

    public MessController(MessService mess, AuthService auth) {
        this.mess = mess;
        this.auth = auth;
    }

    @PostMapping("/menu")
    public MessMenu addMenu(@RequestHeader("X-User-Id") Long userId, @RequestBody MenuRequest request) {
        auth.require(userId, Warden.class);
        return mess.addMenu(request.menuDate(), request.mealType(), request.items());
    }

    /** date format: 2026-11-15 (defaults to today). */
    @GetMapping("/menu")
    public List<MessMenu> menu(@RequestHeader("X-User-Id") Long userId,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        auth.requireAny(userId);
        return mess.menuFor(date == null ? LocalDate.now() : date);
    }

    @PostMapping("/feedback")
    public MessFeedback feedback(@RequestHeader("X-User-Id") Long userId, @RequestBody FeedbackRequest request) {
        Student student = auth.require(userId, Student.class);
        return mess.addFeedback(student, request.menuId(), request.rating(), request.comment());
    }

    @GetMapping("/feedback")
    public List<MessFeedback> allFeedback(@RequestHeader("X-User-Id") Long userId) {
        auth.require(userId, Warden.class);
        return mess.allFeedback();
    }

    @PutMapping("/preferences")
    public MealPreference setPreference(@RequestHeader("X-User-Id") Long userId, @RequestBody PreferenceRequest request) {
        Student student = auth.require(userId, Student.class);
        return mess.setPreference(student, request.mealType(), request.preference());
    }

    @GetMapping("/preferences/my")
    public List<MealPreference> myPreferences(@RequestHeader("X-User-Id") Long userId) {
        Student student = auth.require(userId, Student.class);
        return mess.preferencesOf(student.getId());
    }
}
