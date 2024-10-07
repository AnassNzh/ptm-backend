package com.anazih.ptmback.customer;

import com.anazih.ptmback.common.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByIdAndStatus(UUID id, Status status);
}
