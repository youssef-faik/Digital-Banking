package com.example.digitalbanking.services;

import com.example.digitalbanking.dtos.*;
import com.example.digitalbanking.exceptions.BalanceNotSufficientException;
import com.example.digitalbanking.exceptions.BankAccountNotFoundException;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BankAccountService {
    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    CustomerDTO getCustomer(Long customerId);
    CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO);
    void deleteCustomer(Long customerId);
    Page<CustomerDTO> listCustomers(Pageable pageable);

    BankAccountDTO getBankAccount(String accountId);
    CurrentAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId);
    SavingAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId);
    Page<BankAccountDTO> bankAccountList(Pageable pageable);

    Page<BankAccountDTO> listAccountsByCustomer(Long customerId, Pageable pageable);

    void debit(DebitDTO debitDTO);
    void credit(CreditDTO creditDTO);
    void transfer(TransferRequestDTO transferRequestDTO);

    Page<AccountOperationDTO> getAccountHistory(String accountId, Pageable pageable);
}
