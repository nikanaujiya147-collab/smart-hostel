package com.hostel.smarthostel.model;

import com.hostel.smarthostel.pattern.state.ComplaintState;
import com.hostel.smarthostel.pattern.state.ComplaintStates;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Context class of the STATE pattern: it stores a ComplaintStatus (persisted) and delegates
 * assign / startWork / resolve to the matching ComplaintState object.
 */
@Entity
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne
    private MaintenanceStaff assignedStaff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintCategory category;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<StatusHistory> history = new ArrayList<>();

    protected Complaint() { }

    public Complaint(Student student, ComplaintCategory category, String description, Priority priority) {
        if (student == null) throw new IllegalArgumentException("Student is required");
        if (category == null) throw new IllegalArgumentException("Category is required");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description is required");
        this.student = student;
        this.category = category;
        this.description = description;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.history.add(new StatusHistory(this, ComplaintStatus.SUBMITTED, "Complaint submitted"));
    }

    // ---- actions delegated to the current State object ----
    public void assign(MaintenanceStaff staff) { currentState().assign(this, staff); }
    public void startWork(String note) { currentState().startWork(this, note); }
    public void resolve(String note) { currentState().resolve(this, note); }

    public ComplaintState currentState() { return ComplaintStates.of(status); }

    // ---- used by the State classes ----
    public void setAssignedStaff(MaintenanceStaff staff) { this.assignedStaff = staff; }

    public void moveTo(ComplaintStatus newStatus, String note) {
        this.status = newStatus;
        this.history.add(new StatusHistory(this, newStatus, note));
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public MaintenanceStaff getAssignedStaff() { return assignedStaff; }
    public ComplaintCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public ComplaintStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<StatusHistory> getHistory() { return Collections.unmodifiableList(history); }
}
