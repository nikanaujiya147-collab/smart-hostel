package com.hostel.smarthostel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("WARDEN")
public class Warden extends User {

    private String employeeId;

    protected Warden() { }

    public Warden(String username, String password, String fullName, String phone, String employeeId) {
        super(username, password, fullName, phone);
        this.employeeId = employeeId;
    }

    @Override public Role getRole() { return Role.WARDEN; }
    @Override public String getDashboardTitle() { return "Warden Dashboard"; }

    public String getEmployeeId() { return employeeId; }
}
