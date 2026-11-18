package com.hostel.smarthostel.pattern.state;

import com.hostel.smarthostel.exception.InvalidStateTransitionException;
import com.hostel.smarthostel.model.Complaint;
import com.hostel.smarthostel.model.MaintenanceStaff;

/** Default behaviour: every action is rejected. Concrete states override what they allow. */
public abstract class AbstractComplaintState implements ComplaintState {

    @Override
    public void assign(Complaint complaint, MaintenanceStaff staff) { throw reject("assign"); }

    @Override
    public void startWork(Complaint complaint, String note) { throw reject("start work on"); }

    @Override
    public void resolve(Complaint complaint, String note) { throw reject("resolve"); }

    private InvalidStateTransitionException reject(String action) {
        return new InvalidStateTransitionException(
                "Cannot " + action + " a complaint that is " + getStatus());
    }
}
