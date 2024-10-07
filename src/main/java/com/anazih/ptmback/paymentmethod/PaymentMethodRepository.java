package com.anazih.ptmback.paymentmethod;

import com.anazih.ptmback.common.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    Optional<PaymentMethod> findByIdAndStatus(UUID id, Status status);
}
