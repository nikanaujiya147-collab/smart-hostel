package com.hostel.smarthostel.pattern.observer;

import com.hostel.smarthostel.model.Complaint;
import com.hostel.smarthostel.model.ComplaintStatus;
import org.springframework.stereotype.Component;

/** A second observer (console log) to show POLYMORPHISM: same interface, different behaviour. */
@Component
public class AuditLogObserver implements ComplaintObserver {

    @Override
    public void onStatusChanged(Complaint complaint, ComplaintStatus oldStatus, ComplaintStatus newStatus) {
        System.out.println("[AUDIT] Complaint #" + complaint.getId() + ": " + oldStatus + " -> " + newStatus);
    }
}
