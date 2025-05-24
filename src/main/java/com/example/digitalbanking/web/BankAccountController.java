package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.BankAccountDTO;
import com.example.digitalbanking.dtos.CreateCurrentAccountRequestDTO;
import com.example.digitalbanking.dtos.CurrentAccountDTO;
import com.example.digitalbanking.dtos.CreateSavingAccountRequestDTO;
import com.example.digitalbanking.dtos.SavingAccountDTO;
import com.example.digitalbanking.exceptions.BankAccountNotFoundException;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import com.example.digitalbanking.services.BankAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        } catch (RuntimeException e) {
            log.error("Error listing accounts for customer ID {}: {}", customerId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error listing accounts for customer", e);
        }
    }
}
