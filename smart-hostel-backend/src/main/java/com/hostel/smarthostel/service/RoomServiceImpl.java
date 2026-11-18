package com.hostel.smarthostel.service;

import com.hostel.smarthostel.exception.BusinessRuleException;
import com.hostel.smarthostel.exception.ResourceNotFoundException;
import com.hostel.smarthostel.model.Room;
import com.hostel.smarthostel.model.RoomAllocation;
import com.hostel.smarthostel.model.Student;
import com.hostel.smarthostel.repository.RoomAllocationRepository;
import com.hostel.smarthostel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository rooms;
    private final RoomAllocationRepository allocations;
    private final AuthService auth;

    public RoomServiceImpl(RoomRepository rooms, RoomAllocationRepository allocations, AuthService auth) {
        this.rooms = rooms;
        this.allocations = allocations;
        this.auth = auth;
    }

    @Override public List<Room> listAll() { return rooms.findAllByOrderByRoomNumberAsc(); }
    @Override public List<Room> listAvailable() { return rooms.findAvailable(); }
    @Override public List<RoomAllocation> activeAllocations() { return allocations.findByActiveTrueOrderByIdAsc(); }

    @Override
    public RoomAllocation allocate(Long studentId, Long roomId) {
        Student student = auth.findById(studentId, Student.class, "Student");
        if (allocations.findByStudentIdAndActiveTrue(studentId).isPresent()) {
            throw new BusinessRuleException("Student already has a room. Use change-room instead.");
        }
        Room room = findRoom(roomId);
        room.assignOccupant();                       // throws RoomFullException if no space
        return allocations.save(new RoomAllocation(student, room));
    }

    @Override
    public RoomAllocation changeRoom(Long studentId, Long newRoomId) {
        RoomAllocation current = allocations.findByStudentIdAndActiveTrue(studentId)
                .orElseThrow(() -> new BusinessRuleException("Student has no active room to change from"));
        if (current.getRoom().getId().equals(newRoomId)) {
            throw new BusinessRuleException("Student is already in this room");
        }
        Room newRoom = findRoom(newRoomId);
        newRoom.assignOccupant();                    // check capacity first, so a failure changes nothing
        current.getRoom().releaseOccupant();
        current.end();
        return allocations.save(new RoomAllocation(current.getStudent(), newRoom));
    }

    @Override
    public RoomAllocation myAllocation(Long studentId) {
        return allocations.findByStudentIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("No room allocated yet"));
    }

    private Room findRoom(Long roomId) {
        return rooms.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room " + roomId + " not found"));
    }
}
