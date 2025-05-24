package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.*;
import com.example.digitalbanking.services.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts") // Base path for account-related endpoints
@AllArgsConstructor
@Slf4j
@Tag(name = "Bank Account API", description = "Endpoints for managing bank accounts and operations")
public class BankAccountRestController {

    private final BankAccountService bankAccountService;

    @Operation(summary = "Get bank account by ID", description = "Retrieves details of a specific bank account (Saving or Current) by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bank account",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {SavingAccountDTO.class, CurrentAccountDTO.class}))),
            @ApiResponse(responseCode = "404", description = "Bank account not found",
                    content = @Content)
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccountDTO> getBankAccount(
            @Parameter(description = "ID of the bank account to retrieve", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef") @PathVariable String accountId) {
        log.info("REST request to get BankAccount : {}", accountId);
        BankAccountDTO bankAccountDTO = bankAccountService.getBankAccount(accountId);
        return ResponseEntity.ok(bankAccountDTO);
    }

    @Operation(summary = "List all bank accounts", description = "Retrieves a paginated list of all bank accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))) // Note: Schema might need refinement for Page<BankAccountDTO>
    })
    @GetMapping
    public ResponseEntity<Page<BankAccountDTO>> listAccounts(
            @ParameterObject Pageable pageable) {
        log.info("REST request to get a page of BankAccounts");
        Page<BankAccountDTO> page = bankAccountService.bankAccountList(pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get account operation history", description = "Retrieves a paginated list of operations for a specific bank account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved account history",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountHistoryDTO.class))), // Updated schema
            @ApiResponse(responseCode = "404", description = "Bank account not found",
                    content = @Content)
    })
    @GetMapping("/{accountId}/operations")
    public ResponseEntity<AccountHistoryDTO> getAccountHistory( // Updated return type
            @Parameter(description = "ID of the bank account", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef") @PathVariable String accountId,
            @ParameterObject Pageable pageable) {
        log.info("REST request to get account history for accountId: {}", accountId);
        // Updated to pass page number and size, and to match the new return type
        AccountHistoryDTO history = bankAccountService.getAccountHistory(accountId, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Create a new Saving Account", description = "Creates a new saving account linked to an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Saving account created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SavingAccountDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PostMapping("/saving")
    public ResponseEntity<SavingAccountDTO> saveSavingAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details for the new saving account", required = true,
                    content = @Content(schema = @Schema(implementation = CreateSavingAccountRequestDTO.class),
                            examples = @ExampleObject(value = "{\"initialBalance\": 5000.00, \"interestRate\": 1.5, \"customerId\": 1}")))
            @RequestBody CreateSavingAccountRequestDTO requestDTO) {
        log.info("REST request to save SavingAccount : {}", requestDTO);
        SavingAccountDTO savedAccount = bankAccountService.saveSavingBankAccount(
                requestDTO.getInitialBalance(),
                requestDTO.getInterestRate(),
                requestDTO.getCustomerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @Operation(summary = "Create a new Current Account", description = "Creates a new current account linked to an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Current account created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CurrentAccountDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PostMapping("/current")
    public ResponseEntity<CurrentAccountDTO> saveCurrentAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details for the new current account", required = true,
                    content = @Content(schema = @Schema(implementation = CreateCurrentAccountRequestDTO.class),
                            examples = @ExampleObject(value = "{\"initialBalance\": 1000.00, \"overDraft\": 200.00, \"customerId\": 1}")))
            @RequestBody CreateCurrentAccountRequestDTO requestDTO) {
        log.info("REST request to save CurrentAccount : {}", requestDTO);
        CurrentAccountDTO savedAccount = bankAccountService.saveCurrentBankAccount(
                requestDTO.getInitialBalance(),
                requestDTO.getOverDraft(),
                requestDTO.getCustomerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @Operation(summary = "Perform a debit operation", description = "Debits a specified amount from a bank account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Debit operation successful"),
            @ApiResponse(responseCode = "404", description = "Bank account not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient balance",
                    content = @Content)
    })
    @PostMapping("/debit")
    public ResponseEntity<Void> debit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the debit operation", required = true,
                    content = @Content(schema = @Schema(implementation = DebitDTO.class),
                            examples = @ExampleObject(value = "{\"accountId\": \"a1b2c3d4-e5f6-7890-1234-567890abcdef\", \"amount\": 100.00, \"description\": \"ATM Withdrawal\"}")))
            @RequestBody DebitDTO debitDTO) {
        log.info("REST request to perform debit : {}", debitDTO);
        bankAccountService.debit(debitDTO);
        return ResponseEntity.ok().build(); // Or noContent() if preferred
    }

    @Operation(summary = "Perform a credit operation", description = "Credits a specified amount to a bank account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credit operation successful"),
            @ApiResponse(responseCode = "404", description = "Bank account not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PostMapping("/credit")
    public ResponseEntity<Void> credit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the credit operation", required = true,
                    content = @Content(schema = @Schema(implementation = CreditDTO.class),
                            examples = @ExampleObject(value = "{\"accountId\": \"a1b2c3d4-e5f6-7890-1234-567890abcdef\", \"amount\": 50.00, \"description\": \"Cash Deposit\"}")))
            @RequestBody CreditDTO creditDTO) {
        log.info("REST request to perform credit : {}", creditDTO);
        bankAccountService.credit(creditDTO);
        return ResponseEntity.ok().build(); // Or noContent() if preferred
    }

    @Operation(summary = "Perform a transfer between accounts", description = "Transfers a specified amount from a source account to a destination account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer operation successful"),
            @ApiResponse(responseCode = "404", description = "Source or destination bank account not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient balance in source account",
                    content = @Content)
    })
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the transfer operation", required = true,
                    content = @Content(schema = @Schema(implementation = TransferRequestDTO.class),
                            examples = @ExampleObject(value = "{\"accountSourceId\": \"a1b2c3d4-e5f6-7890-1234-567890abcdef\", \"accountDestinationId\": \"fedcba09-8765-4321-0987-654321fedcba\", \"amount\": 75.00, \"description\": \"Internal Transfer\"}")))
            @RequestBody TransferRequestDTO transferRequestDTO) {
        log.info("REST request to perform transfer : {}", transferRequestDTO);
        bankAccountService.transfer(transferRequestDTO);
        return ResponseEntity.ok().build(); // Or noContent() if preferred
    }
}
