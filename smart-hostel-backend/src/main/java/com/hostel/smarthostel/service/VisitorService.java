package com.hostel.smarthostel.service;

import com.hostel.smarthostel.exception.ResourceNotFoundException;
import com.hostel.smarthostel.model.Student;
import com.hostel.smarthostel.model.User;
import com.hostel.smarthostel.model.Visitor;
import com.hostel.smarthostel.repository.VisitorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VisitorService {

    private final VisitorRepository visitors;
    private final AuthService auth;

    public VisitorService(VisitorRepository visitors, AuthService auth) {
        this.visitors = visitors;
        this.auth = auth;
    }

    public Visitor record(Long studentId, String name, String phone, String purpose) {
        Student student = auth.findById(studentId, Student.class, "Student");
        return visitors.save(new Visitor(student, name, phone, purpose));
    }

    public Visitor checkOut(Long visitorId) {
        Visitor visitor = find(visitorId);
        visitor.checkOut();
        return visitor;
    }

    public Visitor verify(Long visitorId, User staff) {
        Visitor visitor = find(visitorId);
        visitor.verify(staff);
        return visitor;
    }

    @Transactional(readOnly = true) public List<Visitor> all() { return visitors.findAllByOrderByIdDesc(); }
    @Transactional(readOnly = true) public List<Visitor> forStudent(Long id) { return visitors.findByStudentIdOrderByIdDesc(id); }

    private Visitor find(Long id) {
        return visitors.findById(id).orElseThrow(() -> new ResourceNotFoundException("Visitor record " + id + " not found"));
    }
}
