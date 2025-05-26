package com.example.digitalbanking.mappers;

import com.example.digitalbanking.dtos.BankAccountDTO;
import com.example.digitalbanking.dtos.CurrentAccountDTO;
import com.example.digitalbanking.dtos.CustomerDTO;
import com.example.digitalbanking.dtos.SavingAccountDTO;
import com.example.digitalbanking.dtos.AccountOperationDTO;
import com.example.digitalbanking.entities.BankAccount;
import com.example.digitalbanking.entities.CurrentAccount;
import com.example.digitalbanking.entities.Customer;
import com.example.digitalbanking.entities.SavingAccount;
import com.example.digitalbanking.entities.AccountOperation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class}) // Integrate with Spring and UserMapper
public interface BankAccountMapper {    // Customer Mapping
    @Mapping(source = "appUser.username", target = "createdByUsername")
    CustomerDTO fromCustomer(Customer customer);
    // Renamed from toCustomer to match usage in CustomerServiceImpl
    // When mapping from DTO to Entity, appUser will be set by the service
    @Mapping(target = "appUser", ignore = true)
    Customer fromCustomerDTO(CustomerDTO customerDTO);

    // Generic BankAccount mapping - for polymorphic handling
    @Mapping(source = "customer", target = "customer")
    @Mapping(target = "type", expression = "java(getAccountType(bankAccount))")
    @Mapping(target = "overdraft", expression = "java(getOverdraft(bankAccount))")
    @Mapping(target = "interestRate", expression = "java(getInterestRate(bankAccount))")
    BankAccountDTO fromBankAccount(BankAccount bankAccount);

    // SavingAccount Mapping
    @Mapping(source = "customer", target = "customer") // Map nested customer
    @Mapping(target = "type", constant = "SAVING") // Set account type
    SavingAccountDTO fromSavingAccount(SavingAccount savingAccount);
    @Mapping(source = "customer", target = "customer") // Map nested customer
    SavingAccount toSavingAccount(SavingAccountDTO savingAccountDTO);

    // CurrentAccount Mapping
    @Mapping(source = "customer", target = "customer") // Map nested customer
    @Mapping(target = "type", constant = "CURRENT") // Set account type
    CurrentAccountDTO fromCurrentAccount(CurrentAccount currentAccount);
    @Mapping(source = "customer", target = "customer") // Map nested customer
    CurrentAccount toCurrentAccount(CurrentAccountDTO currentAccountDTO);

    // AccountOperation Mapping
    @Mapping(source = "bankAccount.id", target = "bankAccountId")
    @Mapping(source = "appUser.username", target = "performedByUsername")
    AccountOperationDTO fromAccountOperation(AccountOperation accountOperation);

    // Helper methods for polymorphic mapping
    default String getAccountType(BankAccount bankAccount) {
        if (bankAccount instanceof CurrentAccount) {
            return "CURRENT";
        } else if (bankAccount instanceof SavingAccount) {
            return "SAVING";
        }
        return null;
    }

    default java.math.BigDecimal getOverdraft(BankAccount bankAccount) {
        if (bankAccount instanceof CurrentAccount currentAccount) {
            return currentAccount.getOverdraft();
        }
        return null;
    }

    default java.math.BigDecimal getInterestRate(BankAccount bankAccount) {
        if (bankAccount instanceof SavingAccount savingAccount) {
            return savingAccount.getInterestRate();
        }
        return null;
    }
}
