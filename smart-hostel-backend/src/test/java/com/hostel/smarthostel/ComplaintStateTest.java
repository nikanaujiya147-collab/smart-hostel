package com.hostel.smarthostel;

import com.hostel.smarthostel.exception.InvalidStateTransitionException;
import com.hostel.smarthostel.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComplaintStateTest {

    private Student student;
    private MaintenanceStaff staff;
    private Complaint complaint;

    @BeforeEach
    void setUp() {
        student = new Student("s1", "pw", "Student One", "1", "R1", "BTech");
        staff = new MaintenanceStaff("m1", "pw", "Staff One", "2", "ELECTRICAL");
        complaint = new Complaint(student, ComplaintCategory.ELECTRICAL, "Fan not working", Priority.HIGH);
    }

    @Test
    void newComplaintIsSubmitted() {
        assertEquals(ComplaintStatus.SUBMITTED, complaint.getStatus());
        assertEquals(1, complaint.getHistory().size());
    }

    @Test
    void fullLifecycleWorks() {
        complaint.assign(staff);
        assertEquals(ComplaintStatus.ASSIGNED, complaint.getStatus());
        complaint.startWork("On my way");
        assertEquals(ComplaintStatus.IN_PROGRESS, complaint.getStatus());
        complaint.resolve("Replaced capacitor");
        assertEquals(ComplaintStatus.RESOLVED, complaint.getStatus());
        assertEquals(4, complaint.getHistory().size());
    }

    @Test
    void cannotResolveBeforeWorkStarts() {
        assertThrows(InvalidStateTransitionException.class, () -> complaint.resolve("done"));
        complaint.assign(staff);
        assertThrows(InvalidStateTransitionException.class, () -> complaint.resolve("done"));
    }

    @Test
    void resolvedIsTerminal() {
        complaint.assign(staff);
        complaint.startWork("go");
        complaint.resolve("fixed");
        assertThrows(InvalidStateTransitionException.class, () -> complaint.assign(staff));
        assertThrows(InvalidStateTransitionException.class, () -> complaint.startWork("again"));
    }

    @Test
    void resolveNeedsDetails() {
        complaint.assign(staff);
        complaint.startWork("go");
        assertThrows(IllegalArgumentException.class, () -> complaint.resolve(" "));
    }
}
