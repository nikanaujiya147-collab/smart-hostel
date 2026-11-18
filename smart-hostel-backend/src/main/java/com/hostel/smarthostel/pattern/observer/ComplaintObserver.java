package com.hostel.smarthostel.pattern.observer;

import com.hostel.smarthostel.model.Complaint;
import com.hostel.smarthostel.model.ComplaintStatus;

/** OBSERVER pattern: anyone interested in complaint status changes implements this. */
public interface ComplaintObserver {
    void onStatusChanged(Complaint complaint, ComplaintStatus oldStatus, ComplaintStatus newStatus);
}
