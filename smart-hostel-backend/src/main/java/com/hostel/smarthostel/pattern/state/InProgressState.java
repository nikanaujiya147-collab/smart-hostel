package com.hostel.smarthostel.pattern.state;

import com.hostel.smarthostel.model.Complaint;
import com.hostel.smarthostel.model.ComplaintStatus;

public class InProgressState extends AbstractComplaintState {
    @Override public ComplaintStatus getStatus() { return ComplaintStatus.IN_PROGRESS; }

    @Override
    public void resolve(Complaint complaint, String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("Resolution details are required");
        }
        complaint.moveTo(ComplaintStatus.RESOLVED, note);
    }
}
