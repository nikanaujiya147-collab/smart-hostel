package com.hostel.smarthostel.pattern.state;

import com.hostel.smarthostel.model.ComplaintStatus;
import com.hostel.smarthostel.model.Complaint;
import com.hostel.smarthostel.model.MaintenanceStaff;

/** STATE pattern: the interface every concrete state implements. */
public interface ComplaintState {
    ComplaintStatus getStatus();
    void assign(Complaint complaint, MaintenanceStaff staff);
    void startWork(Complaint complaint, String note);
    void resolve(Complaint complaint, String note);
}
