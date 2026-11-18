package com.hostel.smarthostel.controller;

import com.hostel.smarthostel.model.Student;
import com.hostel.smarthostel.service.AuthService;
import com.hostel.smarthostel.service.PaymentGatewayService;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fees/{feeId}/online")
public class OnlinePaymentController {
    public record OrderRequest(BigDecimal amount) { }
    public record VerifyRequest(String razorpay_order_id, String razorpay_payment_id, String razorpay_signature) { }
    private final AuthService auth; private final PaymentGatewayService gateway; private final ObjectMapper mapper;
    public OnlinePaymentController(AuthService auth, PaymentGatewayService gateway, ObjectMapper mapper) { this.auth = auth; this.gateway = gateway; this.mapper = mapper; }

    @PostMapping("/order")
    public PaymentGatewayService.OrderDetails order(@RequestHeader("X-User-Id") Long userId, @PathVariable Long feeId,
                                                     @RequestBody(required = false) OrderRequest request) {
        Student student = auth.require(userId, Student.class);
        return gateway.createOrder(feeId, student, request == null ? null : request.amount());
    }

    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestHeader("X-User-Id") Long userId, @PathVariable Long feeId, @RequestBody VerifyRequest request) {
        Student student = auth.require(userId, Student.class);
        var result = gateway.verify(feeId, student, request.razorpay_order_id(), request.razorpay_payment_id(), request.razorpay_signature());
        Map<String, Object> response = mapper.convertValue(result.feeRecord(), Map.class);
        response.put("paymentId", result.paymentId()); response.put("receiptNo", result.receiptNo());
        return response;
    }
}
