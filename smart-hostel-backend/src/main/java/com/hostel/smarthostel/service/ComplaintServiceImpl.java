package com.hostel.smarthostel.service;

import com.hostel.smarthostel.exception.ForbiddenException;
import com.hostel.smarthostel.exception.ResourceNotFoundException;
import com.hostel.smarthostel.model.*;
import com.hostel.smarthostel.pattern.observer.ComplaintObserver;
import com.hostel.smarthostel.pattern.strategy.PriorityStrategy;
import com.hostel.smarthostel.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Uses all three patterns:
 *  - STRATEGY : picks a PriorityStrategy to calculate priority when a complaint is submitted
 *  - STATE    : complaint.assign()/startWork()/resolve() delegate to state objects
 *  - OBSERVER : after every status change all registered ComplaintObservers are notified
 */
@Service
@Transactional
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaints;
    private final AuthService auth;
    private final List<PriorityStrategy> strategies;     // Spring injects every PriorityStrategy bean
    private final List<ComplaintObserver> observers;     // Spring injects every ComplaintObserver bean

    public ComplaintServiceImpl(ComplaintRepository complaints, AuthService auth,
                                List<PriorityStrategy> strategies, List<ComplaintObserver> observers) {
        this.complaints = complaints;
        this.auth = auth;
        this.strategies = strategies;
        this.observers = observers;
    }

    @Override
    public Complaint submit(Student student, ComplaintCategory category, String description, String strategyName) {
        if (category == null) throw new IllegalArgumentException("Category is required");
        PriorityStrategy strategy = chooseStrategy(strategyName);
        Priority priority = strategy.calculate(category, description);
        return complaints.save(new Complaint(student, category, description, priority));
    }

    @Override
    public Complaint assign(Long complaintId, Long staffId) {
        Complaint complaint = get(complaintId);
        MaintenanceStaff staff = auth.findById(staffId, MaintenanceStaff.class, "Maintenance staff");
        ComplaintStatus old = complaint.getStatus();
        complaint.assign(staff);                         // STATE decides if this is allowed
        notifyObservers(complaint, old);
        return complaint;
    }

    @Override
    public Complaint startWork(Long complaintId, MaintenanceStaff staff, String note) {
        Complaint complaint = ownedBy(complaintId, staff);
        ComplaintStatus old = complaint.getStatus();
        complaint.startWork(note);
        notifyObservers(complaint, old);
        return complaint;
    }

    @Override
    public Complaint resolve(Long complaintId, MaintenanceStaff staff, String note) {
        Complaint complaint = ownedBy(complaintId, staff);
        ComplaintStatus old = complaint.getStatus();
        complaint.resolve(note);
        notifyObservers(complaint, old);
        return complaint;
    }

    @Override
    @Transactional(readOnly = true)
    public Complaint get(Long complaintId) {
        return complaints.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint " + complaintId + " not found"));
    }

    @Override @Transactional(readOnly = true)
    public List<Complaint> forStudent(Long studentId) { return complaints.findByStudentIdOrderByIdDesc(studentId); }

    @Override @Transactional(readOnly = true)
    public List<Complaint> assignedTo(Long staffId) { return complaints.findByAssignedStaffIdOrderByIdDesc(staffId); }

    @Override @Transactional(readOnly = true)
    public List<Complaint> all() { return complaints.findAllByOrderByIdDesc(); }

    private Complaint ownedBy(Long complaintId, MaintenanceStaff staff) {
        Complaint complaint = get(complaintId);
        if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(staff.getId())) {
            throw new ForbiddenException("This complaint is not assigned to you");
        }
        return complaint;
    }

    private PriorityStrategy chooseStrategy(String name) {
        String wanted = (name == null || name.isBlank()) ? "CATEGORY" : name.toUpperCase();
        return strategies.stream()
                .filter(s -> s.name().equals(wanted))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown priority strategy: " + name));
    }

    private void notifyObservers(Complaint complaint, ComplaintStatus oldStatus) {
        for (ComplaintObserver observer : observers) {
            observer.onStatusChanged(complaint, oldStatus, complaint.getStatus());
        }
    }
}
