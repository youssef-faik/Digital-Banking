package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.Instant;
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
        Customer customer = new Customer(null, "Operation Test Customer", "operation.test@example.com", null);
        customer = customerRepository.save(customer);

        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(new BigDecimal("1000.0"));
        currentAccount.setCreatedAt(Instant.now());
        currentAccount.setCustomer(customer);
        currentAccount.setOverdraft(new BigDecimal("200.0"));
        bankAccount = bankAccountRepository.save(currentAccount);
    }

    @Test
    public void shouldSaveAccountOperation() {
        AccountOperation operation = new AccountOperation();
        operation.setOperationDate(Instant.now());
        operation.setAmount(new BigDecimal("100.0"));
        operation.setType(OperationType.CREDIT);
        operation.setBankAccount(bankAccount);

        AccountOperation savedOperation = accountOperationRepository.save(operation);
        assertThat(savedOperation).isNotNull();
        assertThat(savedOperation.getId()).isNotNull();
        assertThat(savedOperation.getAmount()).isEqualByComparingTo(new BigDecimal("100.0"));
        assertThat(savedOperation.getType()).isEqualTo(OperationType.CREDIT);
    }

    @Test
    public void shouldFindOperationById() {
        AccountOperation operation = new AccountOperation();
        operation.setOperationDate(Instant.now());
        operation.setAmount(new BigDecimal("50.0"));
        operation.setType(OperationType.DEBIT);
        operation.setBankAccount(bankAccount);
        AccountOperation savedOperation = accountOperationRepository.save(operation);

        Optional<AccountOperation> foundOperation = accountOperationRepository.findById(savedOperation.getId());
        assertThat(foundOperation).isPresent();
        assertThat(foundOperation.get().getId()).isEqualTo(savedOperation.getId());
        assertThat(foundOperation.get().getAmount()).isEqualByComparingTo(new BigDecimal("50.0"));
    }

    @Test
    public void shouldFindAllOperations() {
        AccountOperation op1 = new AccountOperation();
        op1.setOperationDate(Instant.now());
        op1.setAmount(new BigDecimal("200.0"));
        op1.setType(OperationType.CREDIT);
        op1.setBankAccount(bankAccount);
        accountOperationRepository.save(op1);

        AccountOperation op2 = new AccountOperation();
        op2.setOperationDate(Instant.now());
        op2.setAmount(new BigDecimal("75.0"));
        op2.setType(OperationType.DEBIT);
        op2.setBankAccount(bankAccount);
        accountOperationRepository.save(op2);

        List<AccountOperation> operations = accountOperationRepository.findAll();
        assertThat(operations.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldDeleteOperation() {
        AccountOperation operation = new AccountOperation();
        operation.setOperationDate(Instant.now());
        operation.setAmount(new BigDecimal("150.0"));
        operation.setType(OperationType.CREDIT);
        operation.setBankAccount(bankAccount);
        AccountOperation savedOperation = accountOperationRepository.save(operation);
        Long operationId = savedOperation.getId();

        accountOperationRepository.deleteById(operationId);

        Optional<AccountOperation> deletedOperation = accountOperationRepository.findById(operationId);
        assertThat(deletedOperation).isNotPresent();
    }
}
