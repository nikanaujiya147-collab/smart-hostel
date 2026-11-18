package com.hostel.smarthostel.repository;

import com.hostel.smarthostel.model.OnlinePaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OnlinePaymentTransactionRepository extends JpaRepository<OnlinePaymentTransaction, Long> {
    Optional<OnlinePaymentTransaction> findByGatewayOrderId(String orderId);
    boolean existsByGatewayPaymentId(String paymentId);
}
