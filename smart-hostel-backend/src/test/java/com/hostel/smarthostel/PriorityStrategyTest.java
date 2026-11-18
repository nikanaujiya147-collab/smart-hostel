package com.hostel.smarthostel;

import com.hostel.smarthostel.model.ComplaintCategory;
import com.hostel.smarthostel.model.Priority;
import com.hostel.smarthostel.pattern.strategy.CategoryPriorityStrategy;
import com.hostel.smarthostel.pattern.strategy.KeywordPriorityStrategy;
import com.hostel.smarthostel.pattern.strategy.PriorityStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityStrategyTest {

    @Test
    void categoryStrategy() {
        PriorityStrategy s = new CategoryPriorityStrategy();
        assertEquals(Priority.HIGH, s.calculate(ComplaintCategory.ELECTRICAL, "x"));
        assertEquals(Priority.MEDIUM, s.calculate(ComplaintCategory.INTERNET, "x"));
        assertEquals(Priority.LOW, s.calculate(ComplaintCategory.CLEANING, "x"));
    }

    @Test
    void keywordStrategy() {
        PriorityStrategy s = new KeywordPriorityStrategy();
        assertEquals(Priority.CRITICAL, s.calculate(ComplaintCategory.ELECTRICAL, "Sparks from socket"));
        assertEquals(Priority.HIGH, s.calculate(ComplaintCategory.PLUMBING, "Tap leak in bathroom"));
        assertEquals(Priority.LOW, s.calculate(ComplaintCategory.CLEANING, "Dust on shelf"));
    }
}
