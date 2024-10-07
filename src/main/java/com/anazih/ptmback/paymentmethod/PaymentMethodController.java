package com.anazih.ptmback.paymentmethod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/payment-methods")
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<PaymentMethod> createPaymentMethod(@RequestBody PaymentMethod paymentMethod) {
        log.info("Creating payment method for customer : {}", paymentMethod);
        return ResponseEntity.ok(paymentMethodService.createPaymentMethod(paymentMethod));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethod> getPaymentMethod(@PathVariable UUID id) {
        log.info("Fetching payment method with id: {}", id);
        return ResponseEntity.ok(paymentMethodService.getPaymentMethod(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethod> updatePaymentMethod(@PathVariable UUID id, @RequestBody PaymentMethod paymentMethod) {
        log.info("Updating payment method with id: {}, new data: {}", id, paymentMethod);
        return ResponseEntity.ok(paymentMethodService.updatePaymentMethod(id, paymentMethod));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> disablePaymentMethod(@PathVariable UUID id) {
        log.info("Disabling payment method with id: {}", id);
        return ResponseEntity.ok(paymentMethodService.disablePaymentMethod(id));
    }
}