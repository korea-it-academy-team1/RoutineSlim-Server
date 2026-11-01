package org.routineslim.domain.subscription.repository;

import java.time.LocalDate;
import java.util.UUID;

import org.routineslim.domain.subscription.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsBySubscriptionIdAndBilledOn(UUID subscriptionId, LocalDate billedOn);
}
