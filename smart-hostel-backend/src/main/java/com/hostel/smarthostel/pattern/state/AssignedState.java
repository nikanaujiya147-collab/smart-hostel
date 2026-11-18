package com.hostel.smarthostel.pattern.state;

import com.hostel.smarthostel.model.Complaint;
import com.hostel.smarthostel.model.ComplaintStatus;
import com.hostel.smarthostel.model.MaintenanceStaff;

public class AssignedState extends AbstractComplaintState {
    @Override public ComplaintStatus getStatus() { return ComplaintStatus.ASSIGNED; }

    /** Re-assigning to another staff member is allowed while work has not started. */
    @Override
    public void assign(Complaint complaint, MaintenanceStaff staff) {
        if (staff == null) throw new IllegalArgumentException("Maintenance staff is required");
        complaint.setAssignedStaff(staff);
        complaint.moveTo(ComplaintStatus.ASSIGNED, "Re-assigned to " + staff.getFullName());
    }

    @Override
    public void startWork(Complaint complaint, String note) {
        complaint.moveTo(ComplaintStatus.IN_PROGRESS, note == null || note.isBlank() ? "Work started" : note);
    }
}
