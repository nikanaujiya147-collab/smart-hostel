package com.hostel.smarthostel.pattern.state;

import com.hostel.smarthostel.model.ComplaintStatus;

import java.util.EnumMap;
import java.util.Map;

/** Maps the persisted ComplaintStatus to its (stateless) State object. */
public final class ComplaintStates {

    private static final Map<ComplaintStatus, ComplaintState> STATES = new EnumMap<>(ComplaintStatus.class);

    static {
        STATES.put(ComplaintStatus.SUBMITTED, new SubmittedState());
        STATES.put(ComplaintStatus.ASSIGNED, new AssignedState());
        STATES.put(ComplaintStatus.IN_PROGRESS, new InProgressState());
        STATES.put(ComplaintStatus.RESOLVED, new ResolvedState());
    }

    private ComplaintStates() { }

    public static ComplaintState of(ComplaintStatus status) {
        return STATES.get(status);
    }
}
