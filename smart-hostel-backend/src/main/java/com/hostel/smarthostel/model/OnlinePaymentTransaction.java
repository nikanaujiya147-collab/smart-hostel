package com.hostel.smarthostel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "online_payment_transactions")
public class OnlinePaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String gatewayOrderId;
    @Column(unique = true) private String gatewayPaymentId;
    @Column(nullable = false) private Long feeId;
    @Column(nullable = false) private Long studentId;
    @Column(nullable = false) private BigDecimal amount;
    private Long localPaymentId;
    protected OnlinePaymentTransaction() { }
    public OnlinePaymentTransaction(String orderId, Long feeId, Long studentId, BigDecimal amount) {
        this.gatewayOrderId = orderId; this.feeId = feeId; this.studentId = studentId; this.amount = amount;
    }
    public Long getId() { return id; } public String getGatewayOrderId() { return gatewayOrderId; }
    public String getGatewayPaymentId() { return gatewayPaymentId; } public Long getFeeId() { return feeId; }
    public Long getStudentId() { return studentId; } public BigDecimal getAmount() { return amount; }
    public Long getLocalPaymentId() { return localPaymentId; }
    public void markPaid(String paymentId, Long localId) { this.gatewayPaymentId = paymentId; this.localPaymentId = localId; }
}
