package com.example.digitalbanking.services;

import com.example.digitalbanking.dtos.*;
import com.example.digitalbanking.entities.*;
import com.example.digitalbanking.exceptions.BalanceNotSufficientException;
import com.example.digitalbanking.exceptions.BankAccountNotFoundException;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import com.example.digitalbanking.exceptions.CustomerDeletionException;
import com.example.digitalbanking.mappers.BankAccountMapper;
import com.example.digitalbanking.repositories.AccountOperationRepository;
import com.example.digitalbanking.repositories.BankAccountRepository;
import com.example.digitalbanking.repositories.CustomerRepository;
import com.example.digitalbanking.repositories.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate; // Added import for Hibernate.unproxy()
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;

@Service
@Transactional
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {

    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final BankAccountMapper dtoMapper;
    private final AppUserRepository appUserRepository;

    public BankAccountServiceImpl(CustomerRepository customerRepository,
                                  BankAccountRepository bankAccountRepository,
                                  AccountOperationRepository accountOperationRepository,
                                  BankAccountMapper dtoMapper,
                                  AppUserRepository appUserRepository) {
        this.customerRepository = customerRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.accountOperationRepository = accountOperationRepository;
        this.dtoMapper = dtoMapper;
        this.appUserRepository = appUserRepository;
    }

    private AppUser getCurrentAuthenticatedAppUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + username));
    }

    private void checkCustomerOwnership(Customer customer, AppUser appUser) {
        if (customer.getAppUser() == null || !customer.getAppUser().getUserId().equals(appUser.getUserId())) {
            log.warn("User {} attempted to access or modify resources for customer {} owned by a different user or with no owner.", appUser.getUsername(), customer.getId());
            throw new AccessDeniedException("You do not have permission to access or modify resources for this customer.");
        }
    }
      private void checkBankAccountOwnership(BankAccount bankAccount, AppUser appUser) {
        // Allow ADMIN users to access any account
        boolean isAdmin = appUser.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleName()));
        
        if (isAdmin) {
            log.info("Admin user {} accessing bank account {}", appUser.getUsername(), bankAccount.getId());
            return;
        }
        
        // For non-admin users, check ownership
        if (bankAccount.getCustomer() == null || bankAccount.getCustomer().getAppUser() == null || 
            !bankAccount.getCustomer().getAppUser().getUserId().equals(appUser.getUserId())) {
            log.warn("User {} attempted to access or modify bank account {} owned by a different user or with no owner.", appUser.getUsername(), bankAccount.getId());
            throw new AccessDeniedException("You do not have permission to access or modify this bank account.");
        }
    }

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        // This method is now primarily handled by CustomerServiceImpl for direct customer creation.
        // If called via BankAccountService, it implies a customer creation in the context of banking operations.
        log.info("Saving new Customer via BankAccountService (delegating to user-specific logic)");
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        customer.setAppUser(currentUser); // Associate with current user
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) {
        log.info("Fetching customer with ID: {} via BankAccountService", customerId);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        checkCustomerOwnership(customer, currentUser);
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO) {
        log.info("Updating customer with ID: {} via BankAccountService", customerId);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        checkCustomerOwnership(customer, currentUser);
        
        // Update fields from DTO
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        // appUser association remains the same (owner doesn't change via this method)
        Customer updatedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        log.info("Attempting to delete customer with ID: {} via BankAccountService", customerId);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found with ID: " + customerId));
        checkCustomerOwnership(customer, currentUser);

        if (bankAccountRepository.existsByCustomerId(customerId)) {
            throw new CustomerDeletionException("Cannot delete customer with ID: " + customerId + " because they have associated bank accounts.");
        }
        customerRepository.deleteById(customerId);
        log.info("Customer deleted successfully: {}", customerId);
    }

    @Override
    public Page<CustomerDTO> listCustomers(Pageable pageable) {
        // In BankAccountService context, this should list customers relevant to the authenticated user.
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        log.info("Listing customers for user: {}", currentUser.getUsername());
        Page<Customer> customers = customerRepository.findByAppUser(currentUser, pageable);
        return customers.map(dtoMapper::fromCustomer);
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) {
        log.info("Fetching bank account with ID: {}", accountId);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        checkBankAccountOwnership(bankAccount, currentUser);

        if (bankAccount instanceof SavingAccount savingAccount) {
            return dtoMapper.fromSavingAccount(savingAccount);
        } else if (bankAccount instanceof CurrentAccount currentAccount) {
            return dtoMapper.fromCurrentAccount(currentAccount);
        }
        throw new BankAccountNotFoundException("Unknown BankAccount type for ID: " + accountId);
    }

    @Override
    public CurrentAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) {
        log.info("Saving new Current Account for customer ID: {}", customerId);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        checkCustomerOwnership(customer, currentUser); // Ensure user owns the customer for whom account is created

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
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        checkCustomerOwnership(customer, currentUser); // Ensure user owns the customer

        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(Instant.now());
        savingAccount.setBalance(BigDecimal.valueOf(initialBalance));
        savingAccount.setInterestRate(BigDecimal.valueOf(interestRate));
        savingAccount.setCustomer(customer);
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingAccount(savedBankAccount);
    }

    @Override
    public Page<BankAccountDTO> bankAccountList(Pageable pageable) {
        // This should list accounts for customers owned by the authenticated user.
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        log.info("Fetching bank accounts for user: {}", currentUser.getUsername());
        Page<BankAccount> bankAccounts = bankAccountRepository.findByCustomerAppUser(currentUser, pageable);
        return bankAccounts.map(bankAccount -> {
            if (bankAccount instanceof SavingAccount sa) {
                return dtoMapper.fromSavingAccount(sa);
            } else {
                return dtoMapper.fromCurrentAccount((CurrentAccount) bankAccount);
            }
        });
    }

    @Override
    public Page<BankAccountDTO> listAccountsByCustomer(Long customerId, Pageable pageable) {
        log.info("Fetching bank accounts for customer ID: {} by user: {}", customerId, getCurrentAuthenticatedAppUser().getUsername());
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        checkCustomerOwnership(customer, currentUser); // Ensure the current user owns the customer

        Page<BankAccount> bankAccounts = bankAccountRepository.findByCustomerIdAndCustomerAppUser(customerId, currentUser, pageable);
        return bankAccounts.map(bankAccount -> {
            if (bankAccount instanceof SavingAccount sa) {
                return dtoMapper.fromSavingAccount(sa);
            } else if (bankAccount instanceof CurrentAccount ca) {
                return dtoMapper.fromCurrentAccount(ca);
            }
            // Should not happen if data is consistent
            log.warn("Unknown bank account type found for customer {} account id {}", customerId, bankAccount.getId());
            return null; // Or throw an exception, or a generic DTO
        });
    }

    @Override
    public void debit(DebitDTO debitDTO) {
        log.info("Processing debit operation: {}", debitDTO);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        BankAccount bankAccount = bankAccountRepository.findById(debitDTO.getAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        checkBankAccountOwnership(bankAccount, currentUser); // Check ownership before operation

        BigDecimal amount = debitDTO.getAmount();
        if (bankAccount.getBalance().compareTo(amount) < 0) {
            throw new BalanceNotSufficientException("Balance not sufficient");
        }

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setOperationDate(Instant.now());
        accountOperation.setBankAccount(bankAccount);
        accountOperation.setDescription(debitDTO.getDescription());
        accountOperation.setAppUser(currentUser); // Tag operation with the user who performed it
        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(CreditDTO creditDTO) {
        log.info("Processing credit operation: {}", creditDTO);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        BankAccount bankAccount = bankAccountRepository.findById(creditDTO.getAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        checkBankAccountOwnership(bankAccount, currentUser); // Check ownership

        BigDecimal amount = creditDTO.getAmount();

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setOperationDate(Instant.now());
        accountOperation.setBankAccount(bankAccount);
        accountOperation.setDescription(creditDTO.getDescription());
        accountOperation.setAppUser(currentUser);
        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance().add(amount));
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(TransferRequestDTO transferRequestDTO) {
        log.info("Processing transfer operation: {}", transferRequestDTO);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        
        BankAccount sourceAccount = bankAccountRepository.findById(transferRequestDTO.getAccountSourceId())
            .orElseThrow(() -> new BankAccountNotFoundException("Source BankAccount not found"));
        BankAccount destinationAccount = bankAccountRepository.findById(transferRequestDTO.getAccountDestinationId())
            .orElseThrow(() -> new BankAccountNotFoundException("Destination BankAccount not found"));
            
        checkBankAccountOwnership(sourceAccount, currentUser); // User must own the source account
        // For transfers, ownership of destination account might not be required by the same user,
        // but the operation itself is initiated by the current user.

        // Debit from source
        DebitDTO debitInfo = new DebitDTO(
                transferRequestDTO.getAccountSourceId(),
                transferRequestDTO.getAmount(),
                "Transfer to " + transferRequestDTO.getAccountDestinationId() + ": " + transferRequestDTO.getDescription()
        );
        debit(debitInfo); // This will call the enhanced debit method with ownership checks and user tagging

        // Credit to destination
        CreditDTO creditInfo = new CreditDTO(
                transferRequestDTO.getAccountDestinationId(),
                transferRequestDTO.getAmount(),
                "Transfer from " + transferRequestDTO.getAccountSourceId() + ": " + transferRequestDTO.getDescription()
        );
        // The credit operation will be tagged with the current user, even if they don't own the destination account's customer.
        // This is typical for transfers. If stricter rules are needed, add checks for destination account ownership here.
        credit(creditInfo); 
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) {
        log.info("Fetching account history for account ID: {} by user: {}", accountId, getCurrentAuthenticatedAppUser().getUsername());
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not Found"));
        checkBankAccountOwnership(bankAccount, currentUser); // Ensure user owns the account

        Pageable pageable = PageRequest.of(page, size);
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, pageable);
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());

        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        return accountHistoryDTO;
    }    @Override
    public DashboardStatsDTO getDashboardStats() {
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        log.info("Fetching dashboard stats for user: {}", currentUser.getUsername());

        long totalCustomers = customerRepository.countByAppUser(currentUser);
        long totalAccounts = bankAccountRepository.countByCustomerAppUser(currentUser);
        
        // Calculate total balance for the user
        List<BankAccount> userAccounts = bankAccountRepository.findAllByCustomerAppUser(currentUser);
        BigDecimal totalBalance = userAccounts.stream()
                .map(BankAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate average balance
        BigDecimal averageAccountBalance = totalAccounts > 0 ? 
            totalBalance.divide(BigDecimal.valueOf(totalAccounts), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        // Operations today
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long totalOperationsToday = accountOperationRepository
            .countByAppUserAndOperationDateAfter(currentUser, todayStart);
        
        // Operations this week
        Instant weekStart = Instant.now().minus(7, ChronoUnit.DAYS);
        long totalOperationsThisWeek = accountOperationRepository
            .countByAppUserAndOperationDateAfter(currentUser, weekStart);
            
        // Operations this month
        Instant monthStart = Instant.now().minus(30, ChronoUnit.DAYS);
        long totalOperationsThisMonth = accountOperationRepository
            .countByAppUserAndOperationDateAfter(currentUser, monthStart);
          // Count active and suspended accounts
        long activeAccountsCount = userAccounts.stream()
            .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
            .count();
        long suspendedAccountsCount = userAccounts.stream()
            .filter(account -> account.getStatus() == AccountStatus.SUSPENDED)
            .count();
        
        // Recent operations (last 24 hours)
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        long recentOperationsCount = accountOperationRepository
            .countByAppUserAndOperationDateAfter(currentUser, yesterday);
            
        // Calculate credit and debit totals
        List<AccountOperation> allOperations = accountOperationRepository
            .findByAppUserAndOperationDateAfterOrderByOperationDateAsc(currentUser, monthStart);
            
        BigDecimal totalCreditAmount = allOperations.stream()
            .filter(op -> op.getType() == OperationType.CREDIT)
            .map(AccountOperation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalDebitAmount = allOperations.stream()
            .filter(op -> op.getType() == OperationType.DEBIT)
            .map(AccountOperation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Count account types
        long currentAccountsCount = userAccounts.stream()
            .filter(account -> account instanceof CurrentAccount)
            .count();
            
        long savingAccountsCount = userAccounts.stream()
            .filter(account -> account instanceof SavingAccount)
            .count();
        
        // Find highest and lowest balances
        BigDecimal highestAccountBalance = userAccounts.stream()
            .map(BankAccount::getBalance)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
            
        BigDecimal lowestAccountBalance = userAccounts.stream()
            .map(BankAccount::getBalance)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
            
        // Operations in last 24 hours
        long operationsLast24Hours = recentOperationsCount;
        
        // Average daily transaction volume (last 30 days)
        BigDecimal averageDailyTransactionVolume = totalOperationsThisMonth > 0 ?
            totalCreditAmount.add(totalDebitAmount).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        return new DashboardStatsDTO(
                totalCustomers,
                totalAccounts,
                totalBalance,
                totalOperationsToday,
                averageAccountBalance,
                activeAccountsCount,
                suspendedAccountsCount,
                recentOperationsCount,
                totalOperationsThisWeek,
                totalOperationsThisMonth,
                totalCreditAmount,
                totalDebitAmount,
                currentAccountsCount,
                savingAccountsCount,
                highestAccountBalance,
                lowestAccountBalance,
                operationsLast24Hours,
                averageDailyTransactionVolume
        );
    }

    @Override
    public DashboardChartDataDTO getDashboardChartData() {
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        log.info("Fetching dashboard chart data for user: {}", currentUser.getUsername());

        // Example: Operations trend for the last 7 days
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<AccountOperation> recentOperations = accountOperationRepository
                .findByAppUserAndOperationDateAfterOrderByOperationDateAsc(currentUser, sevenDaysAgo);

        Map<LocalDate, Double> dailyTotals = recentOperations.stream()
                .collect(Collectors.groupingBy(
                        op -> op.getOperationDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.summingDouble(op -> op.getAmount().doubleValue()) // Summing amounts
                ));

        List<DataPointDTO> operationsTrend = dailyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DataPointDTO(entry.getKey().toString(), entry.getValue()))
                .collect(Collectors.toList());

        // Example: Account type distribution (Current vs Saving)
        List<BankAccount> userAccounts = bankAccountRepository.findAllByCustomerAppUser(currentUser);
        
        log.info("User accounts found for distribution chart: {}", userAccounts.size());
        userAccounts.forEach(acc -> {
            if (acc != null) {
                Object unproxiedAcc = Hibernate.unproxy(acc); // Unproxy before logging type
                String customerName = (acc.getCustomer() != null) ? acc.getCustomer().getName() : "N/A";
                log.info("Account ID: {}, Type: {}, Customer: {}", acc.getId(), unproxiedAcc.getClass().getSimpleName(), customerName);
            } else {
                log.info("Found a null account in userAccounts list.");
            }
        });

        long currentAccountsCount = userAccounts.stream()
                .filter(acc -> acc != null && Hibernate.unproxy(acc) instanceof CurrentAccount) // Use Hibernate.unproxy()
                .count();
        long savingAccountsCount = userAccounts.stream()
                .filter(acc -> acc != null && Hibernate.unproxy(acc) instanceof SavingAccount) // Use Hibernate.unproxy()
                .count();
        
        log.info("Counted Current Accounts: {}", currentAccountsCount);
        log.info("Counted Saving Accounts: {}", savingAccountsCount);

        List<DataPointDTO> accountTypeDistribution = new ArrayList<>();
        accountTypeDistribution.add(new DataPointDTO("Current Accounts", (double) currentAccountsCount));
        accountTypeDistribution.add(new DataPointDTO("Saving Accounts", (double) savingAccountsCount));
        return new DashboardChartDataDTO(operationsTrend, accountTypeDistribution);
    }

    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public List<RecentTransactionDTO> getRecentTransactions(int limit) {
        log.info("Getting recent transactions with limit: {}", limit);
        
        AppUser currentUser = getCurrentUser();
        List<BankAccount> userAccounts = bankAccountRepository.findAllByCustomerAppUser(currentUser);
        List<String> accountIds = userAccounts.stream().map(BankAccount::getId).collect(Collectors.toList());
        
        if (accountIds.isEmpty()) {
            return new ArrayList<>();
        }
          Pageable pageable = PageRequest.of(0, limit);
        Page<AccountOperation> operations = accountOperationRepository.findByBankAccountIdInOrderByOperationDateDesc(accountIds, pageable);        return operations.getContent().stream().map(operation -> {
            BankAccount account = operation.getBankAccount();
            String customerName = account != null ? account.getCustomer().getName() : "Unknown";
            
            return new RecentTransactionDTO(
                    operation.getId(),
                    operation.getType().toString(),
                    operation.getAmount(),
                    operation.getDescription(),
                    LocalDateTime.ofInstant(operation.getOperationDate(), ZoneId.systemDefault()),
                    account != null ? account.getId() : "Unknown",
                    customerName
            );
        }).collect(Collectors.toList());
    }

    @Override
    public FinancialMetricsDTO getFinancialMetrics() {
        log.info("Calculating financial metrics for current user");
        
        AppUser currentUser = getCurrentUser();
        List<BankAccount> userAccounts = bankAccountRepository.findAllByCustomerAppUser(currentUser);
        List<String> accountIds = userAccounts.stream().map(BankAccount::getId).collect(Collectors.toList());
        
        if (accountIds.isEmpty()) {
            return new FinancialMetricsDTO(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
        }
        
        List<AccountOperation> allOperations = accountOperationRepository.findByBankAccountIdIn(accountIds);
        
        // Calculate totals
        BigDecimal totalRevenue = allOperations.stream()
                .filter(op -> op.getType() == OperationType.CREDIT)
                .map(AccountOperation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = allOperations.stream()
                .filter(op -> op.getType() == OperationType.DEBIT)
                .map(AccountOperation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        
        // Calculate growth rate (simple month-over-month)
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        Instant oneMonthAgoInstant = oneMonthAgo.atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        BigDecimal currentMonthRevenue = allOperations.stream()
                .filter(op -> op.getType() == OperationType.CREDIT)
                .filter(op -> op.getOperationDate().isAfter(oneMonthAgoInstant))
                .map(AccountOperation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal previousMonthRevenue = allOperations.stream()
                .filter(op -> op.getType() == OperationType.CREDIT)
                .filter(op -> op.getOperationDate().isBefore(oneMonthAgoInstant))
                .map(AccountOperation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal growthRate = BigDecimal.ZERO;
        if (previousMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = currentMonthRevenue.subtract(previousMonthRevenue)
                    .divide(previousMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        // Simulate transaction fees (typically a small percentage)
        BigDecimal transactionFees = totalRevenue.multiply(BigDecimal.valueOf(0.001)); // 0.1% fee
        
        // Calculate average transaction size
        BigDecimal averageTransactionSize = BigDecimal.ZERO;
        if (!allOperations.isEmpty()) {
            BigDecimal totalTransactionAmount = allOperations.stream()
                    .map(AccountOperation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageTransactionSize = totalTransactionAmount.divide(BigDecimal.valueOf(allOperations.size()), 2, RoundingMode.HALF_UP);
        }
        
        return new FinancialMetricsDTO(
                totalRevenue,
                totalExpenses,
                netProfit,
                growthRate,
                transactionFees,
                averageTransactionSize
        );
    }    @Override
    public BankAccountDTO updateBankAccount(String accountId, BankAccountUpdateDTO updateDTO) {
        log.info("Updating bank account with ID: {}", accountId);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        checkBankAccountOwnership(bankAccount, currentUser);

        // Update common fields
        if (updateDTO.getBalance() != null) {
            bankAccount.setBalance(updateDTO.getBalance());
        }
        
        // Update status if provided
        if (updateDTO.getStatus() != null) {
            try {
                AccountStatus accountStatus = AccountStatus.valueOf(updateDTO.getStatus().toUpperCase());
                bankAccount.setStatus(accountStatus);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value: " + updateDTO.getStatus() + ". Must be ACTIVE or SUSPENDED");
            }
        }

        // Update type-specific fields
        if (bankAccount instanceof CurrentAccount currentAccount) {
            if (updateDTO.getOverdraft() != null) {
                currentAccount.setOverdraft(updateDTO.getOverdraft());
            }
            BankAccount updatedAccount = bankAccountRepository.save(currentAccount);
            return dtoMapper.fromCurrentAccount((CurrentAccount) updatedAccount);
        } else if (bankAccount instanceof SavingAccount savingAccount) {
            if (updateDTO.getInterestRate() != null) {
                savingAccount.setInterestRate(updateDTO.getInterestRate());
            }
            BankAccount updatedAccount = bankAccountRepository.save(savingAccount);
            return dtoMapper.fromSavingAccount((SavingAccount) updatedAccount);
        }
        
        throw new BankAccountNotFoundException("Unknown BankAccount type for ID: " + accountId);
    }

    @Override
    public void deleteBankAccount(String accountId) {
        log.info("Deleting bank account with ID: {}", accountId);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        checkBankAccountOwnership(bankAccount, currentUser);

        // Check if account has operations
        if (bankAccount.getAccountOperations() != null && !bankAccount.getAccountOperations().isEmpty()) {
            throw new RuntimeException("Cannot delete account with existing operations. Account ID: " + accountId);
        }

        bankAccountRepository.delete(bankAccount);
        log.info("Bank account deleted successfully: {}", accountId);
    }    @Override
    public BankAccountDTO toggleAccountStatus(String accountId, String status) {
        log.info("Toggling account status for ID: {} to {}", accountId, status);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        checkBankAccountOwnership(bankAccount, currentUser);

        // Validate and set status
        try {
            AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
            bankAccount.setStatus(accountStatus);
            BankAccount updatedAccount = bankAccountRepository.save(bankAccount);
            
            if (updatedAccount instanceof SavingAccount savingAccount) {
                return dtoMapper.fromSavingAccount(savingAccount);
            } else if (updatedAccount instanceof CurrentAccount currentAccount) {
                return dtoMapper.fromCurrentAccount(currentAccount);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value: " + status + ". Must be ACTIVE or SUSPENDED");
        }
        
        throw new BankAccountNotFoundException("Unknown BankAccount type for ID: " + accountId);
    }

    @Override
    public Page<AccountOperationDTO> getAllUserOperations(int page, int size) {
        log.info("Getting all operations for current user with page: {}, size: {}", page, size);
        
        AppUser currentUser = getCurrentUser();
        List<BankAccount> userAccounts = bankAccountRepository.findAllByCustomerAppUser(currentUser);
        List<String> accountIds = userAccounts.stream().map(BankAccount::getId).collect(Collectors.toList());
        
        if (accountIds.isEmpty()) {
            return Page.empty();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountOperation> operations = accountOperationRepository.findByBankAccountIdInOrderByOperationDateDesc(accountIds, pageable);
        
        return operations.map(dtoMapper::fromAccountOperation);
    }
}
