package com.hostel.smarthostel.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class MessMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate menuDate;

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @Column(length = 500)
    private String items;

    protected MessMenu() { }

    public MessMenu(LocalDate menuDate, MealType mealType, String items) {
        if (menuDate == null || mealType == null) throw new IllegalArgumentException("Date and meal type are required");
        if (items == null || items.isBlank()) throw new IllegalArgumentException("Menu items are required");
        this.menuDate = menuDate;
        this.mealType = mealType;
        this.items = items;
    }

    public Long getId() { return id; }
    public LocalDate getMenuDate() { return menuDate; }
    public MealType getMealType() { return mealType; }
    public String getItems() { return items; }
}
