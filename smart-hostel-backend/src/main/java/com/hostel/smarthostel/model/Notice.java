package com.hostel.smarthostel.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    private NoticeCategory category;

    private LocalDate publishedOn;
    private LocalDate expiryDate;

    @ManyToOne
    private User postedBy;

    protected Notice() { }

    public Notice(String title, String content, NoticeCategory category, LocalDate expiryDate, User postedBy) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (category == null) throw new IllegalArgumentException("Category is required");
        this.title = title;
        this.content = content;
        this.category = category;
        this.publishedOn = LocalDate.now();
        if (expiryDate != null && expiryDate.isBefore(publishedOn)) {
            throw new IllegalArgumentException("Expiry date cannot be in the past");
        }
        this.expiryDate = expiryDate;
        this.postedBy = postedBy;
    }

    /** Business rule lives inside the class: a notice with no expiry never expires. */
    public boolean isActiveOn(LocalDate day) {
        return expiryDate == null || !expiryDate.isBefore(day);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public NoticeCategory getCategory() { return category; }
    public LocalDate getPublishedOn() { return publishedOn; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getPostedByName() { return postedBy == null ? null : postedBy.getFullName(); }
}
