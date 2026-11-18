package com.hostel.smarthostel.model;

import com.hostel.smarthostel.exception.BusinessRuleException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    @Column(nullable = false)
    private String visitorName;

    private String visitorPhone;
    private String purpose;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private boolean verified;

    @ManyToOne
    private User verifiedBy;

    protected Visitor() { }

    public Visitor(Student student, String visitorName, String visitorPhone, String purpose) {
        if (student == null) throw new IllegalArgumentException("Student is required");
        if (visitorName == null || visitorName.isBlank()) throw new IllegalArgumentException("Visitor name is required");
        this.student = student;
        this.visitorName = visitorName;
        this.visitorPhone = visitorPhone;
        this.purpose = purpose;
        this.checkIn = LocalDateTime.now();
    }

    public void checkOut() {
        if (checkOut != null) throw new BusinessRuleException("Visitor already checked out");
        this.checkOut = LocalDateTime.now();
    }

    public void verify(User staff) {
        if (verified) throw new BusinessRuleException("Visit already verified");
        this.verified = true;
        this.verifiedBy = staff;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public String getVisitorName() { return visitorName; }
    public String getVisitorPhone() { return visitorPhone; }
    public String getPurpose() { return purpose; }
    public LocalDateTime getCheckIn() { return checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public boolean isVerified() { return verified; }
    public String getVerifiedByName() { return verifiedBy == null ? null : verifiedBy.getFullName(); }
}
