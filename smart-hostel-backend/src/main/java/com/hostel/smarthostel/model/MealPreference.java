package com.hostel.smarthostel.model;

import jakarta.persistence.*;

@Entity
public class MealPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    private String preference;   // e.g. VEG, NON_VEG, JAIN

    protected MealPreference() { }

    public MealPreference(Student student, MealType mealType, String preference) {
        this.student = student;
        this.mealType = mealType;
        this.preference = preference;
    }

    public void changePreference(String preference) { this.preference = preference; }

    public Long getId() { return id; }
    public MealType getMealType() { return mealType; }
    public String getPreference() { return preference; }
}
