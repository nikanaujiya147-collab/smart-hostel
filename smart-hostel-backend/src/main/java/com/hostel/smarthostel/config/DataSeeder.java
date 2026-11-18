package com.hostel.smarthostel.config;

import com.hostel.smarthostel.model.*;
import com.hostel.smarthostel.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Inserts demo data on first start so the demo needs no registration. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final HostelRepository hostels;
    private final FeeRecordRepository fees;
    private final NoticeRepository notices;
    private final MessMenuRepository menus;

    public DataSeeder(UserRepository users, HostelRepository hostels, FeeRecordRepository fees,
                      NoticeRepository notices, MessMenuRepository menus) {
        this.users = users;
        this.hostels = hostels;
        this.fees = fees;
        this.notices = notices;
        this.menus = menus;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;

        Student s1 = users.save(new Student("student1", "pass", "Aman Kumar", "9000000001", "R101", "B.Tech"));
        users.save(new Student("student2", "pass", "Riya Singh", "9000000005", "R102", "B.Sc"));
        Warden warden = users.save(new Warden("warden1", "pass", "Mr. Sharma", "9000000002", "W01"));
        users.save(new MaintenanceStaff("staff1", "pass", "Ravi Electrician", "9000000003", "ELECTRICAL"));
        users.save(new Accountant("acc1", "pass", "Ms. Gupta", "9000000004", "A01"));

        Hostel hostel = new Hostel("Sunrise Boys Hostel", "Prayagraj");
        hostel.addRoom("101", 2);
        hostel.addRoom("102", 2);
        hostel.addRoom("103", 3);
        hostel.addRoom("104", 1);
        hostels.save(hostel);                         // rooms are saved too (composition cascade)

        fees.save(new FeeRecord(s1, "Semester hostel fee", new BigDecimal("25000"), LocalDate.now().plusDays(30)));
        notices.save(new Notice("Welcome", "Hostel gate closes at 10 PM.", NoticeCategory.GENERAL,
                LocalDate.now().plusDays(60), warden));
        menus.save(new MessMenu(LocalDate.now(), MealType.BREAKFAST, "Poha, Tea"));
        menus.save(new MessMenu(LocalDate.now(), MealType.LUNCH, "Dal, Rice, Roti, Sabzi"));
        menus.save(new MessMenu(LocalDate.now(), MealType.DINNER, "Paneer, Roti, Rice"));

        System.out.println("=== Demo data created. Login users: student1, student2, warden1, staff1, acc1 (password: pass) ===");
    }
}
