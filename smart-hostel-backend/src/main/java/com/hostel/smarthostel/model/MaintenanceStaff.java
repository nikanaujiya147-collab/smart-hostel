package com.hostel.smarthostel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MAINTENANCE_STAFF")
public class MaintenanceStaff extends User {

    private String specialization;

    protected MaintenanceStaff() { }

    public MaintenanceStaff(String username, String password, String fullName, String phone,
                            String specialization) {
        super(username, password, fullName, phone);
        this.specialization = specialization;
    }

    @Override public Role getRole() { return Role.MAINTENANCE_STAFF; }
    @Override public String getDashboardTitle() { return "Maintenance Dashboard"; }

    public String getSpecialization() { return specialization; }
}
