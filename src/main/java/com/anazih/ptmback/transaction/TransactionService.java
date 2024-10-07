package com.anazih.ptmback.transaction;

import com.anazih.ptmback.account.Account;
import com.anazih.ptmback.account.AccountService;
import com.anazih.ptmback.auditlog.AuditLogService;
import com.anazih.ptmback.common.EntityType;
import com.anazih.ptmback.common.EventType;
import com.anazih.ptmback.paymentmethod.PaymentMethod;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;
    private final AccountService accountService;

    @Transactional
    public Transaction initiateTransaction(Transaction transaction) {
        log.info("Initiating transaction: {}", transaction);
        if (transaction.getPaymentMethod() == null || transaction.getPaymentMethod().getId() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (transaction.getAccount() == null || transaction.getAccount().getId() == null) {
            throw new IllegalArgumentException("Account is required");
        }
        var savedTransaction = transactionRepository.save(Transaction.builder().amount(transaction.getAmount()).currency(transaction.getCurrency()).account(Account.builder().id(transaction.getAccount().getId()).build()).paymentMethod(PaymentMethod.builder().id(transaction.getPaymentMethod().getId()).build()).status(TransactionStatus.PENDING).description(transaction.getDescription()).build());

        auditLogService.createAuditLog(EventType.CREATE.name(), EntityType.Transaction.name(), savedTransaction.getId(), null, format("Transaction %s created", savedTransaction.getId()));
        log.info("Transaction created: {}", savedTransaction.getId());
        return savedTransaction;
    }

    @Transactional
    public Transaction processTransaction(UUID id) {
        log.info("Processing transaction with ID: {}", id);
        Transaction transaction = getTransactionById(id);
        if (!transaction.getStatus().equals(TransactionStatus.PENDING)) {
            throw new IllegalArgumentException("Transaction cannot be processed");
        }
        if ((transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) || (transaction.getAccount().getBalance().compareTo(transaction.getAmount()) < 0)) {
            transaction.setStatus(TransactionStatus.FAILED);
        } else {
            transaction.setStatus(TransactionStatus.COMPLETED);
            var account = transaction.getAccount();
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            var updatedAccount = accountService.updateAccount(account.getId(), account);
            transaction.setAccount(updatedAccount);
        }
        transactionRepository.save(transaction);
        log.info("Transaction processed: {}", transaction.getId());

        auditLogService.createAuditLog(EventType.UPDATE.name(), EntityType.Transaction.name(), transaction.getId(), null, format("Transaction %s processed", transaction.getId()));
        return transaction;
    }

    public Transaction getTransactionById(UUID id) {
        log.info("Fetching transaction with ID: {}", id);
        return transactionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
    }

    public Page<Transaction> getAllTransactions(UUID transactionId, TransactionStatus status, UUID accountId, UUID customerId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        log.info("Fetching all transactions with ID: {}, status: {}, account ID: {}, customer ID: {}, from date: {}, to date: {}", transactionId, status, accountId, customerId, fromDate, toDate);
        return transactionRepository.findFilteredTransactions(transactionId, status, accountId, customerId, fromDate, toDate, pageable);

    }

    @Transactional
    public void cancelTransaction(UUID id) {
        log.info("Cancelling transaction with ID: {}", id);
        Transaction transaction = getTransactionById(id);
        if (!transaction.getStatus().equals(TransactionStatus.PENDING)) {
            throw new IllegalArgumentException("Transaction cannot be cancelled");
        }
        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);
        log.info("Transaction cancelled: {}", transaction.getId());
        auditLogService.createAuditLog(EventType.DELETE.name(), EntityType.Transaction.name(), transaction.getId(), null, format("Transaction %s created", transaction.getId()));
    }
}