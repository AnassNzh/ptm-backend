package com.anazih.ptmback.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t " +
            "WHERE (:transactionId IS NULL OR t.id = :transactionId) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:accountId IS NULL OR t.account.id = :accountId) " +
            "AND (:customerId IS NULL OR t.paymentMethod.customer.id = :customerId) " +
            "AND (:fromDate IS NULL OR t.transactionDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.transactionDate <= :toDate)")
    Page<Transaction> findFilteredTransactions(
            @Param("transactionId") UUID transactionId,
            @Param("status") TransactionStatus status,
            @Param("accountId") UUID accountId,
            @Param("customerId") UUID customerId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
}