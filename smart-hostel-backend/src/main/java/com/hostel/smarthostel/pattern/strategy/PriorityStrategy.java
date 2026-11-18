package com.hostel.smarthostel.pattern.strategy;

import com.hostel.smarthostel.model.ComplaintCategory;
import com.hostel.smarthostel.model.Priority;

/** STRATEGY pattern: interchangeable algorithms for deciding complaint priority. */
public interface PriorityStrategy {
    String name();
    Priority calculate(ComplaintCategory category, String description);
}
