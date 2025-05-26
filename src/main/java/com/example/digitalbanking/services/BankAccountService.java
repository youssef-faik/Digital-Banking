package com.example.digitalbanking.services;

import com.example.digitalbanking.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface BankAccountService {
    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    CustomerDTO getCustomer(Long customerId);
    CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO);
    void deleteCustomer(Long customerId);
    Page<CustomerDTO> listCustomers(Pageable pageable);    BankAccountDTO getBankAccount(String accountId);
    CurrentAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId);
    SavingAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId);
    Page<BankAccountDTO> bankAccountList(Pageable pageable);

    Page<BankAccountDTO> listAccountsByCustomer(Long customerId, Pageable pageable);
    
    // Account Update and Delete operations
    BankAccountDTO updateBankAccount(String accountId, BankAccountUpdateDTO updateDTO);
    void deleteBankAccount(String accountId);
    BankAccountDTO toggleAccountStatus(String accountId, String status);

    void debit(DebitDTO debitDTO);
    void credit(CreditDTO creditDTO);    void transfer(TransferRequestDTO transferRequestDTO);

    // Corrected signature to match implementation
    AccountHistoryDTO getAccountHistory(String accountId, int page, int size);
    DashboardStatsDTO getDashboardStats(); // New method for dashboard statistics
    DashboardChartDataDTO getDashboardChartData(); // New method for dashboard chart data
      // Enhanced dashboard methods
    List<RecentTransactionDTO> getRecentTransactions(int limit);
    FinancialMetricsDTO getFinancialMetrics();
    
    // Operations methods
    Page<AccountOperationDTO> getAllUserOperations(int page, int size);
}
