package com.hostel.smarthostel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** COMPOSITION: a Payment belongs to exactly one FeeRecord and is created only by it. */
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    private FeeRecord feeRecord;

    private BigDecimal amount;
    private String method;
    private LocalDateTime paidOn;

    @Column(unique = true)
    private String receiptNo;

    protected Payment() { }

    Payment(FeeRecord feeRecord, BigDecimal amount, String method) {
        this.feeRecord = feeRecord;
        this.amount = amount;
        this.method = method;
        this.paidOn = LocalDateTime.now();
        this.receiptNo = "RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getMethod() { return method; }
    public LocalDateTime getPaidOn() { return paidOn; }
    public String getReceiptNo() { return receiptNo; }
    public FeeRecord feeRecord() { return feeRecord; }
}
