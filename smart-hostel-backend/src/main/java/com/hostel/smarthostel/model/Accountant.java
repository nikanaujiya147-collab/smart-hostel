package com.hostel.smarthostel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ACCOUNTANT")
public class Accountant extends User {

    private String employeeId;

    protected Accountant() { }

    public Accountant(String username, String password, String fullName, String phone, String employeeId) {
        super(username, password, fullName, phone);
        this.employeeId = employeeId;
    }

    @Override public Role getRole() { return Role.ACCOUNTANT; }
    @Override public String getDashboardTitle() { return "Accountant Dashboard"; }

    public String getEmployeeId() { return employeeId; }
}
