package com.hostel.smarthostel.pattern.state;

import com.hostel.smarthostel.model.ComplaintStatus;

/** Terminal state: inherits the default behaviour, so every action is rejected. */
public class ResolvedState extends AbstractComplaintState {
    @Override public ComplaintStatus getStatus() { return ComplaintStatus.RESOLVED; }
}
