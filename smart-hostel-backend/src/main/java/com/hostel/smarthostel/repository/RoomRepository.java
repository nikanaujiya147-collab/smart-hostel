package com.hostel.smarthostel.repository;

import com.hostel.smarthostel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("select r from Room r where r.occupied < r.capacity order by r.roomNumber")
    List<Room> findAvailable();
    List<Room> findAllByOrderByRoomNumberAsc();
}
