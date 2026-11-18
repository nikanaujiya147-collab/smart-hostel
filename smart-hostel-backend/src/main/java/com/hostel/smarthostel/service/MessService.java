package com.hostel.smarthostel.service;

import com.hostel.smarthostel.exception.ResourceNotFoundException;
import com.hostel.smarthostel.model.*;
import com.hostel.smarthostel.repository.MealPreferenceRepository;
import com.hostel.smarthostel.repository.MessFeedbackRepository;
import com.hostel.smarthostel.repository.MessMenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MessService {

    private final MessMenuRepository menus;
    private final MessFeedbackRepository feedback;
    private final MealPreferenceRepository preferences;

    public MessService(MessMenuRepository menus, MessFeedbackRepository feedback, MealPreferenceRepository preferences) {
        this.menus = menus;
        this.feedback = feedback;
        this.preferences = preferences;
    }

    public MessMenu addMenu(LocalDate date, MealType mealType, String items) {
        return menus.save(new MessMenu(date, mealType, items));
    }

    @Transactional(readOnly = true)
    public List<MessMenu> menuFor(LocalDate date) { return menus.findByMenuDateOrderByIdAsc(date); }

    public MessFeedback addFeedback(Student student, Long menuId, int rating, String comment) {
        MessMenu menu = menus.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu " + menuId + " not found"));
        return feedback.save(new MessFeedback(student, menu, rating, comment));
    }

    @Transactional(readOnly = true)
    public List<MessFeedback> allFeedback() { return feedback.findAllByOrderByIdDesc(); }

    public MealPreference setPreference(Student student, MealType mealType, String preference) {
        if (mealType == null || preference == null || preference.isBlank()) {
            throw new IllegalArgumentException("Meal type and preference are required");
        }
        MealPreference pref = preferences.findByStudentIdAndMealType(student.getId(), mealType)
                .orElseGet(() -> new MealPreference(student, mealType, preference));
        pref.changePreference(preference);
        return preferences.save(pref);
    }

    @Transactional(readOnly = true)
    public List<MealPreference> preferencesOf(Long studentId) { return preferences.findByStudentId(studentId); }
}
