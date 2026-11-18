package com.hostel.smarthostel.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * COMPOSITION: a Hostel owns its Rooms. Rooms are created only through addRoom()
 * and are deleted with the hostel (cascade ALL + orphanRemoval).
 */
@Entity
public class Hostel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    protected Hostel() { }

    public Hostel(String name, String address) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Hostel name is required");
        this.name = name;
        this.address = address;
    }

    public Room addRoom(String roomNumber, int capacity) {
        Room room = new Room(this, roomNumber, capacity);
        rooms.add(room);
        return room;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }
}
