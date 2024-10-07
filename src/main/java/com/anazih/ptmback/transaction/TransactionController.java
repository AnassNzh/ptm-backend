package com.anazih.ptmback.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> initiateTransaction(@RequestBody Transaction transaction) {
        log.info("Initiating transaction: {}", transaction);
        return ResponseEntity.ok(transactionService.initiateTransaction(transaction));
    }

    @PutMapping("/{id}/process")
    public ResponseEntity<Transaction> processTransaction(@PathVariable UUID id) {
        log.info("Processing transaction with ID: {}", id);
        return ResponseEntity.ok(transactionService.processTransaction(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable UUID id) {
        log.info("Fetching transaction with ID: {}", id);
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Transaction>> getAllTransactions(
            @RequestParam(required = false) UUID transactionId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            Pageable pageable) {

        Page<Transaction> transactions = transactionService.getAllTransactions(transactionId, status, accountId, customerId, fromDate, toDate, pageable);
        return ResponseEntity.ok(transactions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTransaction(@PathVariable UUID id) {
        log.info("Cancelling transaction with ID: {}", id);
        transactionService.cancelTransaction(id);
        return ResponseEntity.noContent().build();
    }

}