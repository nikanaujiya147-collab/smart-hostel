package com.hostel.smarthostel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** COMPOSITION: a history entry cannot exist without its Complaint. */
@Entity
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    private String note;
    private LocalDateTime changedAt;

    protected StatusHistory() { }

    StatusHistory(Complaint complaint, ComplaintStatus status, String note) {
        this.complaint = complaint;
        this.status = status;
        this.note = note;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public ComplaintStatus getStatus() { return status; }
    public String getNote() { return note; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
