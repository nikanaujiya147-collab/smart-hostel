package com.hostel.smarthostel;

import com.hostel.smarthostel.exception.BusinessRuleException;
import com.hostel.smarthostel.exception.RoomFullException;
import com.hostel.smarthostel.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RoomAndFeeTest {

    @Test
    void roomEnforcesCapacity() {
        Hostel hostel = new Hostel("H", "addr");
        Room room = hostel.addRoom("101", 2);
        room.assignOccupant();
        room.assignOccupant();
        assertTrue(room.isFull());
        assertThrows(RoomFullException.class, room::assignOccupant);
        room.releaseOccupant();
        assertEquals(1, room.getAvailableBeds());
    }

    @Test
    void hostelOwnsRooms() {
        Hostel hostel = new Hostel("H", "addr");
        hostel.addRoom("101", 2);
        assertEquals(1, hostel.getRooms().size());
    }

    @Test
    void feeStatusIsDerivedFromPayments() {
        Student s = new Student("s", "pw", "S", "1", "R", "C");
        FeeRecord fee = new FeeRecord(s, "Sem", new BigDecimal("1000"), LocalDate.now().plusDays(10));
        assertEquals("PENDING", fee.getStatus());
        fee.addPayment(new BigDecimal("400"), "CASH");
        assertEquals("PARTIAL", fee.getStatus());
        fee.addPayment(new BigDecimal("600"), "UPI");
        assertEquals("PAID", fee.getStatus());
        assertEquals(2, fee.getPayments().size());
    }

    @Test
    void cannotOverpay() {
        Student s = new Student("s", "pw", "S", "1", "R", "C");
        FeeRecord fee = new FeeRecord(s, "Sem", new BigDecimal("1000"), LocalDate.now().plusDays(10));
        assertThrows(BusinessRuleException.class, () -> fee.addPayment(new BigDecimal("1500"), "CASH"));
    }

    @Test
    void noticeExpiry() {
        Notice n = new Notice("T", "c", NoticeCategory.GENERAL, LocalDate.now().plusDays(1), null);
        assertTrue(n.isActiveOn(LocalDate.now()));
        assertFalse(n.isActiveOn(LocalDate.now().plusDays(2)));
    }

    @Test
    void userPolymorphism() {
        User[] users = {
            new Student("a", "p", "A", "1", "R", "C"),
            new Warden("b", "p", "B", "1", "W"),
            new MaintenanceStaff("c", "p", "C", "1", "PLUMBING"),
            new Accountant("d", "p", "D", "1", "AC")
        };
        assertEquals(Role.STUDENT, users[0].getRole());
        assertEquals("Warden Dashboard", users[1].getDashboardTitle());
        assertEquals(Role.MAINTENANCE_STAFF, users[2].getRole());
        assertEquals(Role.ACCOUNTANT, users[3].getRole());
    }
}
