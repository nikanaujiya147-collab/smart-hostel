package com.hostel.smarthostel;

import com.hostel.smarthostel.model.*;
import com.hostel.smarthostel.repository.*;
import com.hostel.smarthostel.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentGatewayTest {
    @Test void acceptsValidSignatureAndRejectsInvalidSignature() throws Exception {
        assertTrue(PaymentGatewayService.verifySignature("order_1", "pay_1", signature("order_1|pay_1", "secret"), "secret"));
        assertFalse(PaymentGatewayService.verifySignature("order_1", "pay_1", "bad", "secret"));
    }
    @Test void rejectsOverpaymentAtFeeDomainBoundary() {
        Student student = new Student("s", "p", "Student", "1", "R1", "IT");
        FeeRecord fee = new FeeRecord(student, "Hostel fee", new BigDecimal("100.00"), null);
        assertThrows(RuntimeException.class, () -> fee.addPayment(new BigDecimal("100.01"), "ONLINE"));
    }
    private String signature(String data, String secret) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(bytes);
    }
    @Test void repeatedVerifiedPaymentReturnsExistingPaymentInsteadOfRecordingAgain() {
        FeeRecordRepository fees = mock(FeeRecordRepository.class);
        OnlinePaymentTransactionRepository txs = mock(OnlinePaymentTransactionRepository.class);
        PaymentRepository payments = mock(PaymentRepository.class);
        FeeService feeService = mock(FeeService.class);
        PaymentGatewayService gateway = new PaymentGatewayService(fees, txs, payments, feeService);
        ReflectionTestUtils.setField(gateway, "keyId", "rzp_test_key");
        ReflectionTestUtils.setField(gateway, "keySecret", "secret");
        Student student = mock(Student.class); when(student.getId()).thenReturn(9L);
        FeeRecord fee = new FeeRecord(student, "Hostel fee", new BigDecimal("100.00"), null);
        OnlinePaymentTransaction tx = new OnlinePaymentTransaction("order_1", 7L, 9L, new BigDecimal("100.00"));
        tx.markPaid("pay_1", 22L);
        Payment payment = mock(Payment.class);
        when(txs.findByGatewayOrderId("order_1")).thenReturn(Optional.of(tx));
        when(txs.existsByGatewayPaymentId("pay_1")).thenReturn(true);
        when(payments.findById(22L)).thenReturn(Optional.of(payment));
        when(payment.getId()).thenReturn(22L); when(payment.getReceiptNo()).thenReturn("RCPT-TEST");
        when(fees.findById(7L)).thenReturn(Optional.of(fee));
        var result = gateway.verify(7L, student, "order_1", "pay_1", "unused");
        assertEquals(22L, result.paymentId());
        assertEquals("RCPT-TEST", result.receiptNo());
        verify(feeService, never()).pay(anyLong(), any(), anyString());
    }
}
