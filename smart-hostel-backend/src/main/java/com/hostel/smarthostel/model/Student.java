package com.hostel.smarthostel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    private String rollNumber;
    private String course;

    protected Student() { }

    public Student(String username, String password, String fullName, String phone,
                   String rollNumber, String course) {
        super(username, password, fullName, phone);
        this.rollNumber = rollNumber;
        this.course = course;
    }

    @Override public Role getRole() { return Role.STUDENT; }
    @Override public String getDashboardTitle() { return "Student Dashboard"; }

    public String getRollNumber() { return rollNumber; }
    public String getCourse() { return course; }
}
