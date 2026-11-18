package com.hostel.smarthostel.pattern.state;

import com.hostel.smarthostel.model.Complaint;
import com.hostel.smarthostel.model.ComplaintStatus;
import com.hostel.smarthostel.model.MaintenanceStaff;

public class SubmittedState extends AbstractComplaintState {
    @Override public ComplaintStatus getStatus() { return ComplaintStatus.SUBMITTED; }

    @Override
    public void assign(Complaint complaint, MaintenanceStaff staff) {
        if (staff == null) throw new IllegalArgumentException("Maintenance staff is required");
        complaint.setAssignedStaff(staff);
        complaint.moveTo(ComplaintStatus.ASSIGNED, "Assigned to " + staff.getFullName());
    }
}
