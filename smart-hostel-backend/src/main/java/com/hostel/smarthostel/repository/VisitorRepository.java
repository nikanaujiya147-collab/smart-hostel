package com.hostel.smarthostel.repository;

import com.hostel.smarthostel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    List<Visitor> findByStudentIdOrderByIdDesc(Long studentId);
    List<Visitor> findAllByOrderByIdDesc();
}
