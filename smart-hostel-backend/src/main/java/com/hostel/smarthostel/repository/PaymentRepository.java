package com.hostel.smarthostel.repository;

import com.hostel.smarthostel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    java.util.Optional<Payment> findFirstByFeeRecordIdAndMethodAndAmountOrderByIdDesc(Long feeId, String method, java.math.BigDecimal amount);

}
