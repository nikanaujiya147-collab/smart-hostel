package com.hostel.smarthostel.model;

import com.hostel.smarthostel.exception.BusinessRuleException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Fee owed by a student. Paid/pending status is DERIVED from payments (never stored twice). */
@Entity
public class FeeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    private String description;
    private BigDecimal totalAmount;
    private LocalDate dueDate;

    @OneToMany(mappedBy = "feeRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Payment> payments = new ArrayList<>();

    protected FeeRecord() { }

    public FeeRecord(Student student, String description, BigDecimal totalAmount, LocalDate dueDate) {
        if (student == null) throw new IllegalArgumentException("Student is required");
        if (totalAmount == null || totalAmount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.student = student;
        this.description = description;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
    }

    public Payment addPayment(BigDecimal amount, String method) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Payment must be positive");
        if (amount.compareTo(getPendingAmount()) > 0) {
            throw new BusinessRuleException("Payment exceeds pending amount of " + getPendingAmount());
        }
        Payment payment = new Payment(this, amount, method);
        payments.add(payment);
        return payment;
    }

    public BigDecimal getPaidAmount() {
        return payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPendingAmount() { return totalAmount.subtract(getPaidAmount()); }

    public String getStatus() {
        if (getPendingAmount().signum() == 0) return "PAID";
        return getPaidAmount().signum() == 0 ? "PENDING" : "PARTIAL";
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public String getDescription() { return description; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public List<Payment> getPayments() { return Collections.unmodifiableList(payments); }
}
