package com.hostel.smarthostel.service;

import com.hostel.smarthostel.model.Complaint;
import com.hostel.smarthostel.model.ComplaintCategory;
import com.hostel.smarthostel.model.MaintenanceStaff;
import com.hostel.smarthostel.model.Student;

import java.util.List;

public interface ComplaintService {
    Complaint submit(Student student, ComplaintCategory category, String description, String strategyName);
    Complaint assign(Long complaintId, Long staffId);
    Complaint startWork(Long complaintId, MaintenanceStaff staff, String note);
    Complaint resolve(Long complaintId, MaintenanceStaff staff, String note);
    Complaint get(Long complaintId);
    List<Complaint> forStudent(Long studentId);
    List<Complaint> assignedTo(Long staffId);
    List<Complaint> all();
}
