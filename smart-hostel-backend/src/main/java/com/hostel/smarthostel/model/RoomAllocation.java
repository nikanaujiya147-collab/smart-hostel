package com.hostel.smarthostel.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * ASSOCIATION class between Student and Room. A student exists independently of a room
 * (AGGREGATION-like), and this record keeps allocation history.
 */
@Entity
public class RoomAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private Room room;

    private LocalDate allocatedOn;
    private LocalDate endedOn;
    private boolean active;

    protected RoomAllocation() { }

    public RoomAllocation(Student student, Room room) {
        this.student = student;
        this.room = room;
        this.allocatedOn = LocalDate.now();
        this.active = true;
    }

    public void end() {
        this.active = false;
        this.endedOn = LocalDate.now();
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Room getRoom() { return room; }
    public LocalDate getAllocatedOn() { return allocatedOn; }
    public LocalDate getEndedOn() { return endedOn; }
    public boolean isActive() { return active; }
}
