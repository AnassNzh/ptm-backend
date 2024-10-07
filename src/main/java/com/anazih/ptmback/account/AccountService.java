package com.anazih.ptmback.account;

import com.anazih.ptmback.auditlog.AuditLogService;
import com.anazih.ptmback.common.EntityType;
import com.anazih.ptmback.common.EventType;
import com.anazih.ptmback.customer.Customer;
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
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Account createAccount(Account account) {
        log.info("Creating account: {}", account.getAccountNumber());
        if (account.getCustomer() == null && account.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        var savedAccount = accountRepository.save(account);
        auditLogService.createAuditLog(EventType.CREATE.name(), EntityType.Account.name(), savedAccount.getId(), null, format("Account %s created", savedAccount.getAccountNumber()));
        return savedAccount;
    }

    public Account getAccount(UUID accountId) {
        log.info("Retrieving account with ID: {}", accountId);
        return accountRepository.findByIdAndStatus(accountId, AccountStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));
    }

    @Transactional
    public Account updateAccount(UUID accountId, Account account) {
        log.info("Updating account with ID: {}", accountId);

        return accountRepository.findByIdAndStatus(accountId, AccountStatus.ACTIVE).map(existingAccount -> {
            if (existingAccount.getStatus().equals(AccountStatus.CLOSED)) {
                throw new IllegalArgumentException("Account with ID " + accountId + " is closed");
            }
            if (account.getAccountType() != null) {
                existingAccount.setAccountType(account.getAccountType());
            }
            if (account.getCurrency() != null) {
                existingAccount.setCurrency(account.getCurrency());
            }
            if (account.getBalance() != null) {
                existingAccount.setBalance(account.getBalance());
            }
            if (account.getStatus() != null) {
                existingAccount.setStatus(account.getStatus());
            }
            if (account.getCustomer() != null && account.getCustomer().getId() != null) {
                existingAccount.setCustomer(Customer.builder().id(account.getCustomer().getId()).build());
            }
            log.info("Account updated: {}", existingAccount.getAccountNumber());
            auditLogService.createAuditLog(EventType.UPDATE.name(), EntityType.Account.name(), existingAccount.getId(), null, format("Account %s updated", existingAccount.getAccountNumber()));
            return accountRepository.save(existingAccount);
        }).orElseThrow(() -> {
            log.error("Account with ID {} not found", accountId);
            return new EntityNotFoundException("Account not found with ID: " + accountId);
        });
    }

    @Transactional
    public UUID disableAccount(UUID accountId) {
        log.info("Deactivating account with ID: {}", accountId);
        var existingAccount = accountRepository.findByIdAndStatus(accountId, AccountStatus.ACTIVE).orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));
        existingAccount.setStatus(AccountStatus.CLOSED);
        var disabeledAccount = accountRepository.save(existingAccount);
        auditLogService.createAuditLog(EventType.DELETE.name(), EntityType.Account.name(), existingAccount.getId(), null, format("Account %s deleted", existingAccount.getAccountNumber()));
        return disabeledAccount.getId();

    }
}