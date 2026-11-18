package com.hostel.smarthostel.pattern.strategy;

import com.hostel.smarthostel.model.ComplaintCategory;
import com.hostel.smarthostel.model.Priority;
import org.springframework.stereotype.Component;

/** Priority depends only on the complaint category. */
@Component
public class CategoryPriorityStrategy implements PriorityStrategy {

    @Override public String name() { return "CATEGORY"; }

    @Override
    public Priority calculate(ComplaintCategory category, String description) {
        switch (category) {
            case ELECTRICAL:
            case PLUMBING:
                return Priority.HIGH;
            case INTERNET:
                return Priority.MEDIUM;
            default:
                return Priority.LOW;
        }
    }
}
