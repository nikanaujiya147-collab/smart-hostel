package com.hostel.smarthostel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostel.smarthostel.exception.BusinessRuleException;
import com.hostel.smarthostel.exception.OnlinePaymentNotConfiguredException;
import com.hostel.smarthostel.exception.ResourceNotFoundException;
import com.hostel.smarthostel.model.*;
import com.hostel.smarthostel.repository.FeeRecordRepository;
import com.hostel.smarthostel.repository.OnlinePaymentTransactionRepository;
import com.hostel.smarthostel.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

@Service
public class PaymentGatewayService {
    private final FeeRecordRepository fees;
    private final OnlinePaymentTransactionRepository transactions;
    private final PaymentRepository payments;
    private final FeeService feeService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();
    @Value("${razorpay.key-id:}") private String keyId;
    @Value("${razorpay.key-secret:}") private String keySecret;

    public record OrderDetails(String orderId, long amount, String currency, String keyId) { }
    public record VerifyResult(FeeRecord feeRecord, Long paymentId, String receiptNo) { }

    public PaymentGatewayService(FeeRecordRepository fees, OnlinePaymentTransactionRepository transactions,
                                 PaymentRepository payments, FeeService feeService) {
        this.fees = fees; this.transactions = transactions; this.payments = payments; this.feeService = feeService;
    }

    public boolean configured() { return keyId != null && !keyId.isBlank() && keySecret != null && !keySecret.isBlank(); }

    @Transactional
    public OrderDetails createOrder(Long feeId, Student student, BigDecimal requestedAmount) {
        requireConfigured();
        FeeRecord fee = fees.findById(feeId).orElseThrow(() -> new ResourceNotFoundException("Fee record " + feeId + " not found"));
        if (!fee.getStudent().getId().equals(student.getId())) throw new BusinessRuleException("This fee does not belong to the calling student");
        BigDecimal pending = fee.getPendingAmount();
        BigDecimal amount = requestedAmount == null ? pending : requestedAmount;
        if (amount.signum() <= 0 || amount.compareTo(pending) > 0) throw new BusinessRuleException("Payment amount must be greater than zero and no more than pending amount " + pending);
        if (amount.scale() > 2) throw new BusinessRuleException("Payment amount can have at most two decimal places");
        long paise = amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("amount", paise); payload.put("currency", "INR"); payload.put("receipt", "FEE-" + feeId);
            HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(keyId, keySecret);
            ResponseEntity<String> response = rest.exchange("https://api.razorpay.com/v1/orders", HttpMethod.POST,
                    new HttpEntity<>(mapper.writeValueAsString(payload), headers), String.class);
            JsonNode json = mapper.readTree(response.getBody());
            String orderId = json.path("id").asText();
            if (orderId.isBlank()) throw new IllegalStateException("Razorpay did not return an order id");
            transactions.save(new OnlinePaymentTransaction(orderId, feeId, student.getId(), amount));
            return new OrderDetails(orderId, paise, "INR", keyId);
        } catch (Exception ex) {
            throw new BusinessRuleException("Unable to create Razorpay order: " + ex.getMessage());
        }
    }

    @Transactional
    public VerifyResult verify(Long feeId, Student student, String orderId, String paymentId, String signature) {
        requireConfigured();
        if (blank(orderId) || blank(paymentId) || blank(signature)) throw new IllegalArgumentException("Razorpay verification details are required");
        OnlinePaymentTransaction tx = transactions.findByGatewayOrderId(orderId)
                .orElseThrow(() -> new BusinessRuleException("Unknown online payment order"));
        if (!tx.getFeeId().equals(feeId) || !tx.getStudentId().equals(student.getId())) throw new BusinessRuleException("Payment order does not belong to this student and fee");
        if (transactions.existsByGatewayPaymentId(paymentId)) {
            if (paymentId.equals(tx.getGatewayPaymentId()) && tx.getLocalPaymentId() != null) {
                Payment existing = payments.findById(tx.getLocalPaymentId()).orElseThrow(() -> new ResourceNotFoundException("Recorded payment not found"));
                FeeRecord fee = fees.findById(feeId).orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));
                return new VerifyResult(fee, existing.getId(), existing.getReceiptNo());
            }
            throw new BusinessRuleException("This Razorpay payment has already been recorded");
        }
        if (!verifySignature(orderId, paymentId, signature, keySecret)) throw new BusinessRuleException("Invalid Razorpay payment signature");
        FeeRecord updated = feeService.pay(feeId, tx.getAmount(), "ONLINE");
        Payment payment = payments.findFirstByFeeRecordIdAndMethodAndAmountOrderByIdDesc(feeId, "ONLINE", tx.getAmount())
                .orElseThrow(() -> new IllegalStateException("Payment record was not created"));
        tx.markPaid(paymentId, payment.getId());
        transactions.save(tx);
        return new VerifyResult(updated, payment.getId(), payment.getReceiptNo());
    }

    public static boolean verifySignature(String orderId, String paymentId, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : digest) hex.append(String.format("%02x", value));
            return MessageDigest.isEqual(hex.toString().getBytes(StandardCharsets.UTF_8), signature.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) { return false; }
    }
    private void requireConfigured() { if (!configured()) throw new OnlinePaymentNotConfiguredException(); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
