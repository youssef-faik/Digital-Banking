package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountOperationRepositoryTest {

    @Autowired
    private AccountOperationRepository accountOperationRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer(null, "Operation Test Customer", "operation.test@example.com");
        customer = customerRepository.save(customer);

        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(1000.0);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setCustomer(customer);
        currentAccount.setOverdraft(200.0);
        bankAccount = bankAccountRepository.save(currentAccount);
    }

    @Test
    public void shouldSaveAccountOperation() {
        AccountOperation operation = new AccountOperation();
        operation.setOperationDate(new Date());
        operation.setAmount(100.0);
        operation.setType(OperationType.CREDIT);
        operation.setBankAccount(bankAccount);

        AccountOperation savedOperation = accountOperationRepository.save(operation);
        assertThat(savedOperation).isNotNull();
        assertThat(savedOperation.getId()).isNotNull();
        assertThat(savedOperation.getAmount()).isEqualTo(100.0);
        assertThat(savedOperation.getType()).isEqualTo(OperationType.CREDIT);
    }

    @Test
    public void shouldFindOperationById() {
        AccountOperation operation = new AccountOperation();
        operation.setOperationDate(new Date());
        operation.setAmount(50.0);
        operation.setType(OperationType.DEBIT);
        operation.setBankAccount(bankAccount);
        AccountOperation savedOperation = accountOperationRepository.save(operation);

        Optional<AccountOperation> foundOperation = accountOperationRepository.findById(savedOperation.getId());
        assertThat(foundOperation).isPresent();
        assertThat(foundOperation.get().getId()).isEqualTo(savedOperation.getId());
        assertThat(foundOperation.get().getAmount()).isEqualTo(50.0);
    }

    @Test
    public void shouldFindAllOperations() {
        AccountOperation op1 = new AccountOperation();
        op1.setOperationDate(new Date());
        op1.setAmount(200.0);
        op1.setType(OperationType.CREDIT);
        op1.setBankAccount(bankAccount);
        accountOperationRepository.save(op1);

        AccountOperation op2 = new AccountOperation();
        op2.setOperationDate(new Date());
        op2.setAmount(75.0);
        op2.setType(OperationType.DEBIT);
        op2.setBankAccount(bankAccount);
        accountOperationRepository.save(op2);

        List<AccountOperation> operations = accountOperationRepository.findAll();
        assertThat(operations.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldDeleteOperation() {
        AccountOperation operation = new AccountOperation();
        operation.setOperationDate(new Date());
        operation.setAmount(150.0);
        operation.setType(OperationType.CREDIT);
        operation.setBankAccount(bankAccount);
        AccountOperation savedOperation = accountOperationRepository.save(operation);
        Long operationId = savedOperation.getId();

        accountOperationRepository.deleteById(operationId);

        Optional<AccountOperation> deletedOperation = accountOperationRepository.findById(operationId);
        assertThat(deletedOperation).isNotPresent();
    }
}
