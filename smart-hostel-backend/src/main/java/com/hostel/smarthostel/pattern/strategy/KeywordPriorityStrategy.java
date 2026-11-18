package com.hostel.smarthostel.pattern.strategy;

import com.hostel.smarthostel.model.ComplaintCategory;
import com.hostel.smarthostel.model.Priority;
import org.springframework.stereotype.Component;

/** Priority depends on urgent words found in the description. */
@Component
public class KeywordPriorityStrategy implements PriorityStrategy {

    @Override public String name() { return "KEYWORD"; }

    @Override
    public Priority calculate(ComplaintCategory category, String description) {
        String text = description == null ? "" : description.toLowerCase();
        if (text.contains("fire") || text.contains("spark") || text.contains("short circuit")) {
            return Priority.CRITICAL;
        }
        if (text.contains("leak") || text.contains("flood") || text.contains("urgent")) {
            return Priority.HIGH;
        }
        if (text.contains("slow") || text.contains("not working")) {
            return Priority.MEDIUM;
        }
        return Priority.LOW;
    }
}
