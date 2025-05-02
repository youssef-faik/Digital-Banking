package com.example.digitalbanking.mappers;

import com.example.digitalbanking.dtos.CurrentAccountDTO;
import com.example.digitalbanking.dtos.CustomerDTO;
import com.example.digitalbanking.dtos.SavingAccountDTO;
import com.example.digitalbanking.dtos.AccountOperationDTO;
import com.example.digitalbanking.entities.CurrentAccount;
import com.example.digitalbanking.entities.Customer;
import com.example.digitalbanking.entities.SavingAccount;
import com.example.digitalbanking.entities.AccountOperation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Integrate with Spring for dependency injection
public interface BankAccountMapper {

    // Customer Mapping
    CustomerDTO fromCustomer(Customer customer);
    Customer toCustomer(CustomerDTO customerDTO);

    // SavingAccount Mapping
    @Mapping(source = "customer", target = "customerDTO") // Map nested customer
    SavingAccountDTO fromSavingAccount(SavingAccount savingAccount);
    @Mapping(source = "customerDTO", target = "customer") // Map nested customerDTO
    SavingAccount toSavingAccount(SavingAccountDTO savingAccountDTO);

    // CurrentAccount Mapping
    @Mapping(source = "customer", target = "customerDTO") // Map nested customer
    CurrentAccountDTO fromCurrentAccount(CurrentAccount currentAccount);
    @Mapping(source = "customerDTO", target = "customer") // Map nested customerDTO
    CurrentAccount toCurrentAccount(CurrentAccountDTO currentAccountDTO);

    // AccountOperation Mapping
    AccountOperationDTO fromAccountOperation(AccountOperation accountOperation);
    // No need for toAccountOperation usually, as operations are created internally

    // Note: We might need more complex mapping logic for BankAccountDTO later,
    // especially when dealing with lists containing both types.
    // MapStruct can handle subclass mapping if configured.
}
