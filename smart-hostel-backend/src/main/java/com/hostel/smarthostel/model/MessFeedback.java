package com.hostel.smarthostel.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class MessFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private MessMenu menu;

    private int rating;
    private String comment;
    private LocalDateTime submittedAt;

    protected MessFeedback() { }

    public MessFeedback(Student student, MessMenu menu, int rating, String comment) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be between 1 and 5");
        this.student = student;
        this.menu = menu;
        this.rating = rating;
        this.comment = comment;
        this.submittedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public MessMenu getMenu() { return menu; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
