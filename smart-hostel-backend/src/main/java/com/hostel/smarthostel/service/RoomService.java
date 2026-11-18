package com.hostel.smarthostel.service;

import com.hostel.smarthostel.model.Room;
import com.hostel.smarthostel.model.RoomAllocation;

import java.util.List;

/** ABSTRACTION: controllers depend on this interface, not on the implementation (Dependency Inversion). */
public interface RoomService {
    List<Room> listAll();
    List<Room> listAvailable();
    RoomAllocation allocate(Long studentId, Long roomId);
    RoomAllocation changeRoom(Long studentId, Long newRoomId);
    RoomAllocation myAllocation(Long studentId);
    List<RoomAllocation> activeAllocations();
}
