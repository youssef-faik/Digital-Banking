package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.AccountHistoryDTO;
import com.example.digitalbanking.dtos.AccountOperationDTO;
import com.example.digitalbanking.dtos.BankAccountDTO;
import com.example.digitalbanking.dtos.BankAccountUpdateDTO;
import com.example.digitalbanking.dtos.CreateCurrentAccountRequestDTO;
import com.example.digitalbanking.dtos.CurrentAccountDTO;
import com.example.digitalbanking.dtos.CreateSavingAccountRequestDTO;
import com.example.digitalbanking.dtos.SavingAccountDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.net.URI;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
@Slf4j
@Tag(name = "Bank Account Management", description = "API for bank account operations including creation, transactions, and history")
@SecurityRequirement(name = "bearerAuth")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @Operation(
        summary = "Get all bank accounts", 
        description = "Retrieves a paginated list of all bank accounts"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved bank accounts list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @GetMapping
    public ResponseEntity<Page<BankAccountDTO>> listAccounts(
        @Parameter(description = "Pagination information")
        Pageable pageable
    ) {
        log.info("REST request to get a page of BankAccounts");
        Page<BankAccountDTO> page = bankAccountService.bankAccountList(pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(
        summary = "Get bank account by ID", 
        description = "Retrieves details of a specific bank account by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved bank account",
            content = @Content(schema = @Schema(implementation = BankAccountDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Bank account not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<BankAccountDTO> getBankAccountById(
        @Parameter(description = "ID of the bank account to retrieve", required = true)
        @PathVariable String id
    ) {
        log.info("REST request to get BankAccount : {}", id);
        try {
            BankAccountDTO bankAccountDTO = bankAccountService.getBankAccount(id);
            return ResponseEntity.ok(bankAccountDTO);
        } catch (BankAccountNotFoundException e) {
            log.warn("BankAccount not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Create current account", 
        description = "Creates a new current (checking) account for a customer"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Current account successfully created",
            content = @Content(schema = @Schema(implementation = CurrentAccountDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Customer not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Invalid account data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @PostMapping("/current")
    public ResponseEntity<CurrentAccountDTO> createCurrentAccount(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Current account creation details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreateCurrentAccountRequestDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"initialBalance\": 5000.00,\n" +
                            "  \"overDraft\": 1000.00,\n" +
                            "  \"customerId\": 1\n" +
                            "}"
                )
            )
        )
        @Valid @RequestBody CreateCurrentAccountRequestDTO requestDTO
    ) {
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

    @Operation(
        summary = "Create savings account", 
        description = "Creates a new savings account for a customer"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Savings account successfully created",
            content = @Content(schema = @Schema(implementation = SavingAccountDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Customer not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Invalid account data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @PostMapping("/saving")
    public ResponseEntity<SavingAccountDTO> createSavingAccount(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Savings account creation details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreateSavingAccountRequestDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"initialBalance\": 10000.00,\n" +
                            "  \"interestRate\": 3.5,\n" +
                            "  \"customerId\": 1\n" +
                            "}"
                )
            )
        )
        @Valid @RequestBody CreateSavingAccountRequestDTO requestDTO
    ) {
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

    @Operation(
        summary = "Get accounts by customer", 
        description = "Retrieves all bank accounts owned by a specific customer"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved customer's accounts",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Customer not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Not authorized to access this customer's accounts",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<Page<BankAccountDTO>> listAccountsByCustomer(
        @Parameter(description = "ID of the customer whose accounts to retrieve", required = true)
        @PathVariable Long customerId,
        
        @Parameter(description = "Pagination information")
        Pageable pageable
    ) {
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
    }    @Operation(
        summary = "Get account operations history", 
        description = "Retrieves the transaction history for a specific bank account"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved account operations history",
            content = @Content(schema = @Schema(implementation = AccountHistoryDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Bank account not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Not authorized to access this account's operations",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @GetMapping("/{accountId}/operations")
    public ResponseEntity<AccountHistoryDTO> getAccountOperations(
        @Parameter(description = "ID of the bank account whose operations to retrieve", required = true)
        @PathVariable String accountId,
        
        @Parameter(description = "Pagination information")
        Pageable pageable
    ) {
        log.info("REST request to get operations for account ID: {}", accountId);
        try {
            AccountHistoryDTO page = bankAccountService.getAccountHistory(accountId, pageable.getPageNumber(), pageable.getPageSize());
            return ResponseEntity.ok(page);
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

    @Operation(
        summary = "Debit account", 
        description = "Withdraws funds from a bank account"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Debit operation successful",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Bank account not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Insufficient balance or invalid amount",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Not authorized to debit this account",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @PostMapping("/debit")
    public ResponseEntity<Void> debit(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Debit operation details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = DebitDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"accountId\": \"acc-123456\",\n" +
                            "  \"amount\": 500.00,\n" +
                            "  \"description\": \"ATM Withdrawal\"\n" +
                            "}"
                )
            )
        )
        @Valid @RequestBody DebitDTO debitDTO
    ) {
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

    @Operation(
        summary = "Credit account", 
        description = "Deposits funds into a bank account"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Credit operation successful",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Bank account not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Invalid amount",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Not authorized to credit this account",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @PostMapping("/credit")
    public ResponseEntity<Void> credit(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credit operation details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreditDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"accountId\": \"acc-123456\",\n" +
                            "  \"amount\": 1000.00,\n" +
                            "  \"description\": \"Salary Deposit\"\n" +
                            "}"
                )
            )
        )
        @Valid @RequestBody CreditDTO creditDTO
    ) {
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

    @Operation(
        summary = "Transfer funds", 
        description = "Transfers funds from one bank account to another"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Transfer operation successful",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "One or both bank accounts not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Insufficient balance or invalid amount",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Not authorized to perform this transfer",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Transfer operation details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = TransferRequestDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"accountSourceId\": \"acc-123456\",\n" +
                            "  \"accountDestinationId\": \"acc-789012\",\n" +
                            "  \"amount\": 750.00,\n" +
                            "  \"description\": \"Monthly Rent Payment\"\n" +
                            "}"
                )
            )
        )
        @Valid @RequestBody TransferRequestDTO transferRequestDTO
    ) {
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

    @PutMapping("/{id}")
    public ResponseEntity<BankAccountDTO> updateBankAccount(
            @PathVariable String id,
            @Valid @RequestBody BankAccountUpdateDTO updateDTO) {
        log.info("REST request to update BankAccount : {} with data : {}", id, updateDTO);
        try {
            BankAccountDTO updatedAccount = bankAccountService.updateBankAccount(id, updateDTO);
            return ResponseEntity.ok(updatedAccount);
        } catch (BankAccountNotFoundException e) {
            log.warn("Bank account not found with ID: {} for update", id);
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            log.warn("Access denied for updating account ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error updating account with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating account", e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable String id) {
        log.info("REST request to delete BankAccount : {}", id);
        try {
            bankAccountService.deleteBankAccount(id);
            return ResponseEntity.noContent().build();
        } catch (BankAccountNotFoundException e) {
            log.warn("Bank account not found with ID: {} for deletion", id);
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            log.warn("Access denied for deleting account ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error deleting account with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BankAccountDTO> toggleAccountStatus(
            @PathVariable String id,
            @RequestBody String status) {
        log.info("REST request to change status of BankAccount {} to {}", id, status);
        try {
            BankAccountDTO updatedAccount = bankAccountService.toggleAccountStatus(id, status);
            return ResponseEntity.ok(updatedAccount);
        } catch (BankAccountNotFoundException e) {
            log.warn("Bank account not found with ID: {} for status change", id);
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            log.warn("Access denied for changing status of account ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error changing status of account with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(
        summary = "Get all operations for current user", 
        description = "Retrieves a paginated list of all operations (transactions) for the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved operations list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @GetMapping("/operations")
    public ResponseEntity<Page<AccountOperationDTO>> getAllUserOperations(Pageable pageable) {
        log.info("REST request to get all operations for current user");
        try {
            Page<AccountOperationDTO> operations = bankAccountService.getAllUserOperations(
                pageable.getPageNumber(), 
                pageable.getPageSize()
            );
            return ResponseEntity.ok(operations);
        } catch (AccessDeniedException e) {
            log.warn("Access denied when fetching user operations: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error fetching user operations: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching operations", e);
        }
    }
}
