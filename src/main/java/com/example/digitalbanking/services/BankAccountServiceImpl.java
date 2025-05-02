package com.example.digitalbanking.services;

import com.example.digitalbanking.dtos.*;
import com.example.digitalbanking.entities.*;
import com.example.digitalbanking.exceptions.BalanceNotSufficientException;
import com.example.digitalbanking.exceptions.BankAccountNotFoundException;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import com.example.digitalbanking.exceptions.CustomerDeletionException; // Import new exception
import com.example.digitalbanking.mappers.BankAccountMapper;
import com.example.digitalbanking.repositories.AccountOperationRepository;
import com.example.digitalbanking.repositories.BankAccountRepository;
import com.example.digitalbanking.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor // Constructor injection for dependencies
@Slf4j // Lombok annotation for logging
public class BankAccountServiceImpl implements BankAccountService {

    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final BankAccountMapper dtoMapper; // Renamed for clarity

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer via BankAccountService"); // Added context to log
        // Corrected method name from toCustomer to fromCustomerDTO
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) {
        log.info("Fetching customer with ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override // Corrected signature to match interface (added Pageable, returns Page)
    public Page<CustomerDTO> listCustomers(Pageable pageable) {
        log.info("Fetching customers page: {} size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(dtoMapper::fromCustomer);
    }

    @Override
    public CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO) {
        log.info("Updating customer with ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        // Update fields from DTO
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        Customer updatedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(updatedCustomer);
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) {
         log.info("Fetching bank account with ID: {}", accountId);
         BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));

         // Map based on actual type
         if (bankAccount instanceof SavingAccount savingAccount) {
             return dtoMapper.fromSavingAccount(savingAccount);
         } else if (bankAccount instanceof CurrentAccount currentAccount) {
             return dtoMapper.fromCurrentAccount(currentAccount);
         }
         // Should not happen with current setup, but handle defensively
         throw new BankAccountNotFoundException("Unknown BankAccount type for ID: " + accountId);
    }

    @Override
    public CurrentAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) {
        log.info("Saving new Current Account for customer ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(Instant.now());
        currentAccount.setBalance(BigDecimal.valueOf(initialBalance));
        currentAccount.setOverdraft(BigDecimal.valueOf(overDraft));
        currentAccount.setCustomer(customer);
        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentAccount(savedBankAccount);
    }

    @Override
    public SavingAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) {
         log.info("Saving new Saving Account for customer ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(Instant.now());
        savingAccount.setBalance(BigDecimal.valueOf(initialBalance));
        savingAccount.setInterestRate(BigDecimal.valueOf(interestRate));
        savingAccount.setCustomer(customer);
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingAccount(savedBankAccount);
    }


    @Override // Corrected signature to match interface (added Pageable, returns Page)
    public Page<BankAccountDTO> bankAccountList(Pageable pageable) {
        log.info("Fetching bank accounts page: {} size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<BankAccount> bankAccounts = bankAccountRepository.findAll(pageable);
        return bankAccounts.map(bankAccount -> {
            if (bankAccount instanceof SavingAccount sa) {
                return dtoMapper.fromSavingAccount(sa);
            } else { // Must be CurrentAccount
                return dtoMapper.fromCurrentAccount((CurrentAccount) bankAccount);
            }
        });
    }

    @Override
    public void debit(DebitDTO debitDTO) {
        log.info("Processing debit operation: {}", debitDTO);
        BankAccount bankAccount = bankAccountRepository.findById(debitDTO.getAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));

        BigDecimal amount = debitDTO.getAmount();
        if (bankAccount.getBalance().compareTo(amount) < 0) {
            throw new BalanceNotSufficientException("Balance not sufficient");
        }

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setOperationDate(Instant.now());
        accountOperation.setBankAccount(bankAccount);
        // Consider adding description from DTO if needed in the operation entity
        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
        bankAccountRepository.save(bankAccount); // Balance update
    }

    @Override
    public void credit(CreditDTO creditDTO) {
        log.info("Processing credit operation: {}", creditDTO);
        BankAccount bankAccount = bankAccountRepository.findById(creditDTO.getAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));

        BigDecimal amount = creditDTO.getAmount();

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setOperationDate(Instant.now());
        accountOperation.setBankAccount(bankAccount);
        // Consider adding description from DTO if needed
        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance().add(amount));
        bankAccountRepository.save(bankAccount); // Balance update
    }

    @Override
    public void transfer(TransferRequestDTO transferRequestDTO) {
        log.info("Processing transfer operation: {}", transferRequestDTO);
        // Perform debit on source account
        debit(new DebitDTO(
                transferRequestDTO.getAccountSourceId(),
                transferRequestDTO.getAmount(),
                transferRequestDTO.getDescription() != null ? transferRequestDTO.getDescription() : "Transfer"
        ));
        // Perform credit on destination account
        credit(new CreditDTO(
                transferRequestDTO.getAccountDestinationId(),
                transferRequestDTO.getAmount(),
                transferRequestDTO.getDescription() != null ? transferRequestDTO.getDescription() : "Transfer"
        ));
        // Note: This is not truly atomic. For production, consider Saga pattern or two-phase commit if needed.
    }

    @Override
    public Page<AccountOperationDTO> getAccountHistory(String accountId, Pageable pageable) {
        log.info("Fetching account history for account ID: {} page: {} size: {}", accountId, pageable.getPageNumber(), pageable.getPageSize());
        // Check if account exists first
        if (!bankAccountRepository.existsById(accountId)) {
            throw new BankAccountNotFoundException("BankAccount not found with ID: " + accountId);
        }
        Page<AccountOperation> operations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, pageable);
        return operations.map(dtoMapper::fromAccountOperation);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        log.info("Attempting to delete customer with ID: {}", customerId);
        // Check if customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer Not found with ID: " + customerId);
        }
        // Check if customer has associated bank accounts
        if (bankAccountRepository.existsByCustomerId(customerId)) {
            throw new CustomerDeletionException("Cannot delete customer with ID: " + customerId + " because they have associated bank accounts.");
        }
        // Proceed with deletion
        customerRepository.deleteById(customerId);
        log.info("Customer deleted successfully: {}", customerId);
    }
}
