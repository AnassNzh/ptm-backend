package com.anazih.ptmback.customer;

import com.anazih.ptmback.auditlog.AuditLogService;
import com.anazih.ptmback.common.EntityType;
import com.anazih.ptmback.common.EventType;
import com.anazih.ptmback.common.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;


import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Customer createCustomer(Customer customer) {
        log.info("Creating customer: {}", customer.getName());
        var savedCustomer = customerRepository.save(customer);
        auditLogService.createAuditLog(
                EventType.CREATE.name(),
                EntityType.Customer.name(),
                savedCustomer.getId(),
                null,
                format("Customer %s created", savedCustomer.getName())
        );
        return savedCustomer;
    }

    public Customer getCustomer(UUID accountId) {
        log.info("Retrieving customer with ID: {}", accountId);
        return customerRepository.findByIdAndStatus(accountId, Status.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + accountId));
    }

    @Transactional
    public Customer updateCustomer(UUID customerId, Customer customer) {
        log.info("Updating customer with ID: {}", customerId);

        return customerRepository.findByIdAndStatus(customerId, Status.ACTIVE).map(existingCustomer -> {
            if (existingCustomer.getStatus().equals(Status.INACTIVE)) {
                throw new IllegalArgumentException("Customer with ID " + customerId + " is disabled");
            }
            if (customer.getName() != null) {
                existingCustomer.setName(customer.getName());
            }
            if (customer.getEmail() != null) {
                existingCustomer.setEmail(customer.getEmail());
            }
            if (customer.getPhone() != null) {
                existingCustomer.setPhone(customer.getPhone());
            }
            if (customer.getStatus() != null) {
                existingCustomer.setStatus(customer.getStatus());
            }

            log.info("Customer updated: {}", existingCustomer);
            auditLogService.createAuditLog(
                    EventType.UPDATE.name(),
                    EntityType.Customer.name(),
                    existingCustomer.getId(),
                    null,
                    format("Customer %s updated", existingCustomer.getId())
            );
            return customerRepository.save(existingCustomer);
        }).orElseThrow(() -> {
            log.error("Customer with ID {} not found", customerId);
            return new EntityNotFoundException("Customer not found with ID: " + customerId);
        });
    }

    @Transactional
    public UUID disableCustomer(UUID accountId) {
        log.info("Deactivating customer with ID: {}", accountId);
        var existingCustomer = customerRepository.findByIdAndStatus(accountId, Status.ACTIVE).orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + accountId));
        existingCustomer.setStatus(Status.INACTIVE);
        var disabeledCustomer = customerRepository.save(existingCustomer);
        auditLogService.createAuditLog(
                EventType.DELETE.name(),
                EntityType.Customer.name(),
                existingCustomer.getId(),
                null,
                format("Customer %s deleted", existingCustomer.getId())
        );
        return disabeledCustomer.getId();

    }
    public Map<String, Object> processBatchPayments(UUID customerId) {
        log.info("Processing batch payments for customer with ID: {}", customerId);
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("PROCESS_BATCH_PAYMENTS")
                .declareParameters(
                        new SqlParameter("p_customer_id", Types.VARCHAR),
                        new SqlOutParameter("p_transaction_count", Types.INTEGER),
                        new SqlOutParameter("p_total_amount", Types.DECIMAL),
                        new SqlOutParameter("p_error_code", Types.VARCHAR),
                        new SqlOutParameter("p_error_message", Types.VARCHAR)
                );

        var convertedCustomerId = customerId.toString().replace("-", "").toUpperCase();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_customer_id", convertedCustomerId);

        Map<String, Object> result = jdbcCall.execute(parameters);

        return result;
    }
}
