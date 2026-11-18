package com.hostel.smarthostel.controller;

import com.hostel.smarthostel.model.Room;
import com.hostel.smarthostel.model.RoomAllocation;
import com.hostel.smarthostel.model.Student;
import com.hostel.smarthostel.model.Warden;
import com.hostel.smarthostel.service.AuthService;
import com.hostel.smarthostel.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    public record AllocateRequest(Long studentId, Long roomId) { }
    public record ChangeRoomRequest(Long studentId, Long newRoomId) { }

    private final RoomService rooms;
    private final AuthService auth;

    public RoomController(RoomService rooms, AuthService auth) {
        this.rooms = rooms;
        this.auth = auth;
    }

    @GetMapping
    public List<Room> all(@RequestHeader("X-User-Id") Long userId) {
        auth.requireAny(userId);
        return rooms.listAll();
    }

    @GetMapping("/available")
    public List<Room> available(@RequestHeader("X-User-Id") Long userId) {
        auth.requireAny(userId);
        return rooms.listAvailable();
    }

    @GetMapping("/my")
    public RoomAllocation mine(@RequestHeader("X-User-Id") Long userId) {
        Student student = auth.require(userId, Student.class);
        return rooms.myAllocation(student.getId());
    }

    @GetMapping("/allocations")
    public List<RoomAllocation> allocations(@RequestHeader("X-User-Id") Long userId) {
        auth.require(userId, Warden.class);
        return rooms.activeAllocations();
    }

    @PostMapping("/allocate")
    public RoomAllocation allocate(@RequestHeader("X-User-Id") Long userId, @RequestBody AllocateRequest request) {
        auth.require(userId, Warden.class);
        return rooms.allocate(request.studentId(), request.roomId());
    }

    @PutMapping("/change")
    public RoomAllocation change(@RequestHeader("X-User-Id") Long userId, @RequestBody ChangeRoomRequest request) {
        auth.require(userId, Warden.class);
        return rooms.changeRoom(request.studentId(), request.newRoomId());
    }
}
