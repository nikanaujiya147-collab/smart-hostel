package com.hostel.smarthostel.service;

import com.hostel.smarthostel.exception.ResourceNotFoundException;
import com.hostel.smarthostel.model.FeeRecord;
import com.hostel.smarthostel.model.Payment;
import com.hostel.smarthostel.model.Student;
import com.hostel.smarthostel.repository.FeeRecordRepository;
import com.hostel.smarthostel.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FeeService {

    private final FeeRecordRepository fees;
    private final PaymentRepository payments;
    private final AuthService auth;

    public FeeService(FeeRecordRepository fees, PaymentRepository payments, AuthService auth) {
        this.fees = fees;
        this.payments = payments;
        this.auth = auth;
    }

    public FeeRecord create(Long studentId, String description, BigDecimal amount, LocalDate dueDate) {
        Student student = auth.findById(studentId, Student.class, "Student");
        return fees.save(new FeeRecord(student, description, amount, dueDate));
    }

    public FeeRecord pay(Long feeId, BigDecimal amount, String method) {
        FeeRecord fee = find(feeId);
        fee.addPayment(amount, method);       // business rules are inside FeeRecord
        return fees.save(fee);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> receipt(Long paymentId) {
        Payment payment = payments.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment " + paymentId + " not found"));
        FeeRecord fee = payment.feeRecord();
        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("receiptNo", payment.getReceiptNo());
        receipt.put("student", fee.getStudent().getFullName());
        receipt.put("rollNumber", fee.getStudent().getRollNumber());
        receipt.put("feeDescription", fee.getDescription());
        receipt.put("amountPaid", payment.getAmount());
        receipt.put("method", payment.getMethod());
        receipt.put("paidOn", payment.getPaidOn());
        receipt.put("remainingBalance", fee.getPendingAmount());
        return receipt;
    }

    @Transactional(readOnly = true) public List<FeeRecord> all() { return fees.findAllByOrderByIdDesc(); }
    @Transactional(readOnly = true) public List<FeeRecord> forStudent(Long id) { return fees.findByStudentIdOrderByIdDesc(id); }

    private FeeRecord find(Long id) {
        return fees.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fee record " + id + " not found"));
    }
}
