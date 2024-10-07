package com.anazih.ptmback.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        log.info("Creating customer: {}", customer);
        return ResponseEntity.ok(customerService.createCustomer(customer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable UUID id) {
        log.info("Fetching customer with id: {}", id);
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable UUID id, @RequestBody Customer customer) {
        log.info("Updating customer with id: {}, new data: {}", id, customer);
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> disableCustomer(@PathVariable UUID id) {
        log.info("Disabling customer with id: {}", id);
        return ResponseEntity.ok(customerService.disableCustomer(id));
    }
    @GetMapping("/{customerId}/batch-process")
    public ResponseEntity<Map<String, Object>> batchProcessPayments(@PathVariable UUID customerId) {
        log.info("Batch processing payments for customer with id: {}", customerId);
        Map<String, Object> result = customerService.processBatchPayments(customerId);
        return ResponseEntity.ok(result);
    }
}