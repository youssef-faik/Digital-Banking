package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.*;
import com.example.digitalbanking.services.BankAccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts") // Base path for account-related endpoints
@AllArgsConstructor
@Slf4j
public class BankAccountRestController {

    private final BankAccountService bankAccountService;

    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccountDTO> getBankAccount(@PathVariable String accountId) {
        log.info("REST request to get BankAccount : {}", accountId);
        BankAccountDTO bankAccountDTO = bankAccountService.getBankAccount(accountId);
        return ResponseEntity.ok(bankAccountDTO);
    }

    @GetMapping
    public ResponseEntity<Page<BankAccountDTO>> listAccounts(Pageable pageable) {
        log.info("REST request to get a page of BankAccounts");
        Page<BankAccountDTO> page = bankAccountService.bankAccountList(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{accountId}/operations")
    public ResponseEntity<Page<AccountOperationDTO>> getAccountHistory(
            @PathVariable String accountId,
            Pageable pageable) {
        log.info("REST request to get account history for accountId: {}", accountId);
        Page<AccountOperationDTO> history = bankAccountService.getAccountHistory(accountId, pageable);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/saving")
    public ResponseEntity<SavingAccountDTO> saveSavingAccount(@RequestBody CreateSavingAccountRequestDTO requestDTO) {
        log.info("REST request to save SavingAccount : {}", requestDTO);
        SavingAccountDTO savedAccount = bankAccountService.saveSavingBankAccount(
                requestDTO.getInitialBalance(),
                requestDTO.getInterestRate(),
                requestDTO.getCustomerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @PostMapping("/current")
    public ResponseEntity<CurrentAccountDTO> saveCurrentAccount(@RequestBody CreateCurrentAccountRequestDTO requestDTO) {
        log.info("REST request to save CurrentAccount : {}", requestDTO);
        CurrentAccountDTO savedAccount = bankAccountService.saveCurrentBankAccount(
                requestDTO.getInitialBalance(),
                requestDTO.getOverDraft(),
                requestDTO.getCustomerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @PostMapping("/debit")
    public ResponseEntity<Void> debit(@RequestBody DebitDTO debitDTO) {
        log.info("REST request to perform debit : {}", debitDTO);
        bankAccountService.debit(debitDTO);
        return ResponseEntity.ok().build(); // Or noContent() if preferred
    }

    @PostMapping("/credit")
    public ResponseEntity<Void> credit(@RequestBody CreditDTO creditDTO) {
        log.info("REST request to perform credit : {}", creditDTO);
        bankAccountService.credit(creditDTO);
        return ResponseEntity.ok().build(); // Or noContent() if preferred
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestBody TransferRequestDTO transferRequestDTO) {
        log.info("REST request to perform transfer : {}", transferRequestDTO);
        bankAccountService.transfer(transferRequestDTO);
        return ResponseEntity.ok().build(); // Or noContent() if preferred
    }
}
