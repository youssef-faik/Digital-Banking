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

@Mapper(componentModel = "spring", uses = {UserMapper.class}) // Integrate with Spring and UserMapper
public interface BankAccountMapper {

    // Customer Mapping
    @Mapping(source = "appUser.username", target = "createdByUsername")
    CustomerDTO fromCustomer(Customer customer);
    // Renamed from toCustomer to match usage in CustomerServiceImpl
    // When mapping from DTO to Entity, appUser will be set by the service
    @Mapping(target = "appUser", ignore = true)
    Customer fromCustomerDTO(CustomerDTO customerDTO);

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
    @Mapping(source = "bankAccount.id", target = "bankAccountId")
    @Mapping(source = "appUser.username", target = "performedByUsername")
    AccountOperationDTO fromAccountOperation(AccountOperation accountOperation);
    // No need for toAccountOperation usually, as operations are created internally

    // Note: We might need more complex mapping logic for BankAccountDTO later,
    // especially when dealing with lists containing both types.
    // MapStruct can handle subclass mapping if configured.
}
