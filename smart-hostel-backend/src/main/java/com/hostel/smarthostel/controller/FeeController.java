package com.hostel.smarthostel.controller;

import com.hostel.smarthostel.model.Accountant;
import com.hostel.smarthostel.model.FeeRecord;
import com.hostel.smarthostel.model.Student;
import com.hostel.smarthostel.service.AuthService;
import com.hostel.smarthostel.service.FeeService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    public record CreateFeeRequest(Long studentId, String description, BigDecimal totalAmount, LocalDate dueDate) { }
    public record PaymentRequest(BigDecimal amount, String method) { }

    private final FeeService fees;
    private final AuthService auth;

    public FeeController(FeeService fees, AuthService auth) {
        this.fees = fees;
        this.auth = auth;
    }

    @PostMapping
    public FeeRecord create(@RequestHeader("X-User-Id") Long userId, @RequestBody CreateFeeRequest request) {
        auth.require(userId, Accountant.class);
        return fees.create(request.studentId(), request.description(), request.totalAmount(), request.dueDate());
    }

    @PostMapping("/{feeId}/payments")
    public FeeRecord pay(@RequestHeader("X-User-Id") Long userId, @PathVariable Long feeId,
                         @RequestBody PaymentRequest request) {
        auth.require(userId, Accountant.class);
        return fees.pay(feeId, request.amount(), request.method());
    }

    @GetMapping
    public List<FeeRecord> all(@RequestHeader("X-User-Id") Long userId) {
        auth.require(userId, Accountant.class);
        return fees.all();
    }

    @GetMapping("/student/{studentId}")
    public List<FeeRecord> ofStudent(@RequestHeader("X-User-Id") Long userId, @PathVariable Long studentId) {
        auth.require(userId, Accountant.class);
        return fees.forStudent(studentId);
    }

    @GetMapping("/my")
    public List<FeeRecord> mine(@RequestHeader("X-User-Id") Long userId) {
        Student student = auth.require(userId, Student.class);
        return fees.forStudent(student.getId());
    }

    @GetMapping("/payments/{paymentId}/receipt")
    public Map<String, Object> receipt(@RequestHeader("X-User-Id") Long userId, @PathVariable Long paymentId) {
        auth.requireAny(userId);
        return fees.receipt(paymentId);
    }
}
