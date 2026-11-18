package com.hostel.smarthostel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hostel.smarthostel.exception.RoomFullException;
import jakarta.persistence.*;

/** ENCAPSULATION: occupancy can only change through assignOccupant()/releaseOccupant(). */
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    private Hostel hostel;

    @Column(nullable = false)
    private String roomNumber;

    private int capacity;
    private int occupied;

    protected Room() { }

    Room(Hostel hostel, String roomNumber, int capacity) {
        if (roomNumber == null || roomNumber.isBlank()) throw new IllegalArgumentException("Room number is required");
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least 1");
        this.hostel = hostel;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
    }

    public void assignOccupant() {
        if (isFull()) throw new RoomFullException(roomNumber);
        occupied++;
    }

    public void releaseOccupant() {
        if (occupied > 0) occupied--;
    }

    public boolean isFull() { return occupied >= capacity; }
    public int getAvailableBeds() { return capacity - occupied; }

    public Long getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public int getCapacity() { return capacity; }
    public int getOccupied() { return occupied; }
    public String getHostelName() { return hostel.getName(); }
}
