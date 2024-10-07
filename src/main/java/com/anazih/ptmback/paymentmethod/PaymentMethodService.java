package com.anazih.ptmback.paymentmethod;

import com.anazih.ptmback.auditlog.AuditLogService;
import com.anazih.ptmback.common.EntityType;
import com.anazih.ptmback.common.EventType;
import com.anazih.ptmback.common.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public PaymentMethod createPaymentMethod(PaymentMethod paymentMethod) {
        log.info("Creating payment method for customer : {}", paymentMethod);
        var savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        auditLogService.createAuditLog(
                EventType.CREATE.name(),
                EntityType.PaymentMethod.name(),
                savedPaymentMethod.getId(),
                null,
                format("Payment method for customer %s created", savedPaymentMethod.getCustomer().getId())
        );
        return savedPaymentMethod;
    }

    public PaymentMethod getPaymentMethod(UUID paymentMethodId) {
        log.info("Retrieving payment method with ID: {}", paymentMethodId);
        return paymentMethodRepository.findByIdAndStatus(paymentMethodId, Status.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found with ID: " + paymentMethodId));
    }

    @Transactional
    public PaymentMethod updatePaymentMethod(UUID paymentMethodId, PaymentMethod paymentMethod) {
        log.info("Updating payment method with ID: {}", paymentMethodId);

        return paymentMethodRepository.findByIdAndStatus(paymentMethodId, Status.ACTIVE).map(existingPaymentMethod -> {
            if (existingPaymentMethod.getStatus().equals(Status.INACTIVE)) {
                throw new IllegalArgumentException("Payment method with ID " + paymentMethodId + " is disabled");
            }
            if (paymentMethod.getType() != null) {
                existingPaymentMethod.setType(paymentMethod.getType());
            }
            if (paymentMethod.getProvider() != null) {
                existingPaymentMethod.setProvider(paymentMethod.getProvider());
            }
            if (paymentMethod.getAccountNumber() != null) {
                existingPaymentMethod.setAccountNumber(paymentMethod.getAccountNumber());
            }
            if (paymentMethod.getExpiryDate() != null) {
                existingPaymentMethod.setExpiryDate(paymentMethod.getExpiryDate());
            }
            if (paymentMethod.getStatus() != null) {
                existingPaymentMethod.setStatus(paymentMethod.getStatus());
            }

            log.info("Payment method updated: {}", existingPaymentMethod);
            auditLogService.createAuditLog(
                    EventType.UPDATE.name(),
                    EntityType.PaymentMethod.name(),
                    existingPaymentMethod.getId(),
                    null,
                    format("Payment method %s updated", existingPaymentMethod.getId())
            );
            return paymentMethodRepository.save(existingPaymentMethod);
        }).orElseThrow(() -> {
            log.error("Payment method with ID {} not found", paymentMethodId);
            return new EntityNotFoundException("Payment method not found with ID: " + paymentMethodId);
        });
    }

    @Transactional
    public UUID disablePaymentMethod(UUID paymentMethodId) {
        log.info("Deactivating payment method with ID: {}", paymentMethodId);
        var existingPaymentMethod = paymentMethodRepository.findByIdAndStatus(paymentMethodId, Status.ACTIVE).orElseThrow(() -> new EntityNotFoundException("Payment method not found with ID: " + paymentMethodId));
        existingPaymentMethod.setStatus(Status.INACTIVE);
        var disabeledPaymentMethod = paymentMethodRepository.save(existingPaymentMethod);
        auditLogService.createAuditLog(
                EventType.DELETE.name(),
                EntityType.PaymentMethod.name(),
                existingPaymentMethod.getId(),
                null,
                format("Payment method %s deleted", existingPaymentMethod.getId())
        );
        return disabeledPaymentMethod.getId();

    }
}