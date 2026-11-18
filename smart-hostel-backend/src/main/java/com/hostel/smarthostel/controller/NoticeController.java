package com.hostel.smarthostel.controller;

import com.hostel.smarthostel.model.Notice;
import com.hostel.smarthostel.model.NoticeCategory;
import com.hostel.smarthostel.model.Warden;
import com.hostel.smarthostel.service.AuthService;
import com.hostel.smarthostel.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    public record NoticeRequest(String title, String content, NoticeCategory category, LocalDate expiryDate) { }

    private final NoticeService notices;
    private final AuthService auth;

    public NoticeController(NoticeService notices, AuthService auth) {
        this.notices = notices;
        this.auth = auth;
    }

    @PostMapping
    public Notice publish(@RequestHeader("X-User-Id") Long userId, @RequestBody NoticeRequest request) {
        Warden warden = auth.require(userId, Warden.class);
        return notices.publish(request.title(), request.content(), request.category(), request.expiryDate(), warden);
    }

    /** Anyone logged in sees only notices that have not expired. */
    @GetMapping
    public List<Notice> active(@RequestHeader("X-User-Id") Long userId) {
        auth.requireAny(userId);
        return notices.active();
    }

    @GetMapping("/all")
    public List<Notice> all(@RequestHeader("X-User-Id") Long userId) {
        auth.require(userId, Warden.class);
        return notices.all();
    }
}
