package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.AccountHistoryDTO;
import com.example.digitalbanking.dtos.BankAccountDTO;
import com.example.digitalbanking.dtos.CreateCurrentAccountRequestDTO;
import com.example.digitalbanking.dtos.CurrentAccountDTO;
import com.example.digitalbanking.dtos.CreateSavingAccountRequestDTO;
import com.example.digitalbanking.dtos.SavingAccountDTO;
import com.example.digitalbanking.dtos.AccountOperationDTO;
import com.example.digitalbanking.dtos.DebitDTO;
import com.example.digitalbanking.dtos.CreditDTO;
import com.example.digitalbanking.dtos.TransferRequestDTO;
import com.example.digitalbanking.exceptions.BankAccountNotFoundException;
import com.example.digitalbanking.exceptions.BalanceNotSufficientException;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import com.example.digitalbanking.services.BankAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
@Slf4j
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public ResponseEntity<Page<BankAccountDTO>> listAccounts(Pageable pageable) {
        log.info("REST request to get a page of BankAccounts");
        Page<BankAccountDTO> page = bankAccountService.bankAccountList(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountDTO> getBankAccountById(@PathVariable String id) {
        log.info("REST request to get BankAccount : {}", id);
        try {
            BankAccountDTO bankAccountDTO = bankAccountService.getBankAccount(id);
            return ResponseEntity.ok(bankAccountDTO);
        } catch (BankAccountNotFoundException e) {
            log.warn("BankAccount not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/current")
    public ResponseEntity<CurrentAccountDTO> createCurrentAccount(@Valid @RequestBody CreateCurrentAccountRequestDTO requestDTO) {
        log.info("REST request to create CurrentAccount: {}", requestDTO);
        try {
            CurrentAccountDTO createdAccount = bankAccountService.saveCurrentBankAccount(
                    requestDTO.getInitialBalance(),
                    requestDTO.getOverDraft(),
                    requestDTO.getCustomerId()
            );
            URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/accounts/{id}")
                    .buildAndExpand(createdAccount.getId()).toUri();
            return ResponseEntity.created(location).body(createdAccount);
        } catch (CustomerNotFoundException e) {
            log.warn("Customer not found for creating current account: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + requestDTO.getCustomerId(), e);
        } catch (RuntimeException e) {
            log.error("Error creating current account: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating current account", e);
        }
    }

    @PostMapping("/saving")
    public ResponseEntity<SavingAccountDTO> createSavingAccount(@Valid @RequestBody CreateSavingAccountRequestDTO requestDTO) {
        log.info("REST request to create SavingAccount: {}", requestDTO);
        try {
            SavingAccountDTO createdAccount = bankAccountService.saveSavingBankAccount(
                    requestDTO.getInitialBalance(),
                    requestDTO.getInterestRate(),
                    requestDTO.getCustomerId()
            );
            URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/accounts/{id}")
                    .buildAndExpand(createdAccount.getId()).toUri();
            return ResponseEntity.created(location).body(createdAccount);
        } catch (CustomerNotFoundException e) {
            log.warn("Customer not found for creating saving account: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + requestDTO.getCustomerId(), e);
        } catch (RuntimeException e) {
            log.error("Error creating saving account: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating saving account", e);
        }
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<Page<BankAccountDTO>> listAccountsByCustomer(@PathVariable Long customerId, Pageable pageable) {
        log.info("REST request to get a page of BankAccounts for customer ID: {}", customerId);
        try {
            Page<BankAccountDTO> page = bankAccountService.listAccountsByCustomer(customerId, pageable);
            return ResponseEntity.ok(page);
        } catch (CustomerNotFoundException e) {
            log.warn("Customer not found when listing accounts: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + customerId, e);
        } catch (AccessDeniedException e) {
            log.warn("Access denied when listing accounts for customer ID {}: {}", customerId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error listing accounts for customer ID {}: {}", customerId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error listing accounts for customer", e);
        }
    }

    @GetMapping("/{accountId}/operations")
    public ResponseEntity<AccountHistoryDTO> getAccountOperations( // Updated return type
            @PathVariable String accountId,
            Pageable pageable) {
        log.info("REST request to get operations for account ID: {}", accountId);
        try {
            // Updated to call with page number and size, and to match the new return type
            AccountHistoryDTO history = bankAccountService.getAccountHistory(accountId, pageable.getPageNumber(), pageable.getPageSize());
            return ResponseEntity.ok(history);
        } catch (BankAccountNotFoundException e) {
            log.warn("Bank account not found when fetching operations: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank account not found: " + accountId, e);
        } catch (AccessDeniedException e) {
            log.warn("Access denied when fetching operations for account ID {}: {}", accountId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error fetching operations for account ID {}: {}", accountId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching account operations", e);
        }
    }

    @PostMapping("/debit")
    public ResponseEntity<Void> debit(@Valid @RequestBody DebitDTO debitDTO) {
        log.info("REST request to debit account: {}", debitDTO);
        try {
            bankAccountService.debit(debitDTO);
            return ResponseEntity.ok().build();
        } catch (BankAccountNotFoundException e) {
            log.warn("Bank account not found for debit: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BalanceNotSufficientException e) {
            log.warn("Balance not sufficient for debit: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            log.warn("Access denied for debit operation: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error processing debit: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing debit operation", e);
        }
    }

    @PostMapping("/credit")
    public ResponseEntity<Void> credit(@Valid @RequestBody CreditDTO creditDTO) {
        log.info("REST request to credit account: {}", creditDTO);
        try {
            bankAccountService.credit(creditDTO);
            return ResponseEntity.ok().build();
        } catch (BankAccountNotFoundException e) {
            log.warn("Bank account not found for credit: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            log.warn("Access denied for credit operation: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error processing credit: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing credit operation", e);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequestDTO transferRequestDTO) {
        log.info("REST request to transfer funds: {}", transferRequestDTO);
        try {
            bankAccountService.transfer(transferRequestDTO);
            return ResponseEntity.ok().build();
        } catch (BankAccountNotFoundException e) {
            log.warn("Bank account not found for transfer: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BalanceNotSufficientException e) {
            log.warn("Balance not sufficient for transfer: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            log.warn("Access denied for transfer operation: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error processing transfer: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing transfer operation. Please try again later.", e);
        }
    }
}
