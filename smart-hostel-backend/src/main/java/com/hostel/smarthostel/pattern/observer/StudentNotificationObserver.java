package com.hostel.smarthostel.pattern.observer;

import com.hostel.smarthostel.model.Complaint;
import com.hostel.smarthostel.model.ComplaintStatus;
import com.hostel.smarthostel.model.Notification;
import com.hostel.smarthostel.repository.NotificationRepository;
import org.springframework.stereotype.Component;

/** Saves an in-app notification for the student who raised the complaint. */
@Component
public class StudentNotificationObserver implements ComplaintObserver {

    private final NotificationRepository notifications;

    public StudentNotificationObserver(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Override
    public void onStatusChanged(Complaint complaint, ComplaintStatus oldStatus, ComplaintStatus newStatus) {
        String message = "Your complaint #" + complaint.getId() + " (" + complaint.getCategory()
                + ") changed from " + oldStatus + " to " + newStatus;
        notifications.save(new Notification(complaint.getStudent(), message));
    }
}
