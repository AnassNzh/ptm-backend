package com.anazih.ptmback.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        log.info("Creating account: {}", account);
        return ResponseEntity.ok(accountService.createAccount(account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID id) {
        log.info("Fetching account with id: {}", id);
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable UUID id, @RequestBody Account account) {
        log.info("Updating account with id: {}, new data: {}", id, account);
        return ResponseEntity.ok(accountService.updateAccount(id, account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> disableAccount(@PathVariable UUID id) {
        log.info("Disabling account with id: {}", id);
        return ResponseEntity.ok(accountService.disableAccount(id));

    }
}