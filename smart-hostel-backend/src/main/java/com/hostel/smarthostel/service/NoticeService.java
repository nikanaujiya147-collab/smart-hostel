package com.hostel.smarthostel.service;

import com.hostel.smarthostel.model.Notice;
import com.hostel.smarthostel.model.NoticeCategory;
import com.hostel.smarthostel.model.User;
import com.hostel.smarthostel.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class NoticeService {

    private final NoticeRepository notices;

    public NoticeService(NoticeRepository notices) { this.notices = notices; }

    public Notice publish(String title, String content, NoticeCategory category, LocalDate expiry, User postedBy) {
        return notices.save(new Notice(title, content, category, expiry, postedBy));
    }

    @Transactional(readOnly = true)
    public List<Notice> active() {
        LocalDate today = LocalDate.now();
        return notices.findAllByOrderByIdDesc().stream().filter(n -> n.isActiveOn(today)).toList();
    }

    @Transactional(readOnly = true)
    public List<Notice> all() { return notices.findAllByOrderByIdDesc(); }
}
