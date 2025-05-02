package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.Customer;
import com.example.digitalbanking.entities.SavingAccount;
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
public class SavingAccountRepositoryTest {

    @Autowired
    private SavingAccountRepository savingAccountRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(null, "Saving Test Customer", "saving.test@example.com");
        customer = customerRepository.save(customer);
    }

    @Test
    public void shouldSaveSavingAccount() {
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setBalance(3000.0);
        savingAccount.setCreatedAt(new Date());
        savingAccount.setCustomer(customer);
        savingAccount.setInterestRate(0.03);

        SavingAccount savedAccount = savingAccountRepository.save(savingAccount);
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getInterestRate()).isEqualTo(0.03);
    }

    @Test
    public void shouldFindSavingAccountById() {
        SavingAccount savingAccount = new SavingAccount();
        String accountId = UUID.randomUUID().toString();
        savingAccount.setId(accountId);
        savingAccount.setBalance(4000.0);
        savingAccount.setCreatedAt(new Date());
        savingAccount.setCustomer(customer);
        savingAccount.setInterestRate(0.04);
        savingAccountRepository.save(savingAccount);

        Optional<SavingAccount> foundAccount = savingAccountRepository.findById(accountId);
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getId()).isEqualTo(accountId);
        assertThat(foundAccount.get().getBalance()).isEqualTo(4000.0);
    }

    @Test
    public void shouldFindAllSavingAccounts() {
        SavingAccount account1 = new SavingAccount();
        account1.setId(UUID.randomUUID().toString());
        account1.setBalance(1000.0);
        account1.setCreatedAt(new Date());
        account1.setCustomer(customer);
        account1.setInterestRate(0.02);
        savingAccountRepository.save(account1);

        SavingAccount account2 = new SavingAccount();
        account2.setId(UUID.randomUUID().toString());
        account2.setBalance(2000.0);
        account2.setCreatedAt(new Date());
        account2.setCustomer(customer);
        account2.setInterestRate(0.025);
        savingAccountRepository.save(account2);

        List<SavingAccount> accounts = savingAccountRepository.findAll();
        assertThat(accounts.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldDeleteSavingAccount() {
        SavingAccount savingAccount = new SavingAccount();
        String accountId = UUID.randomUUID().toString();
        savingAccount.setId(accountId);
        savingAccount.setBalance(5000.0);
        savingAccount.setCreatedAt(new Date());
        savingAccount.setCustomer(customer);
        savingAccount.setInterestRate(0.06);
        savingAccountRepository.save(savingAccount);

        savingAccountRepository.deleteById(accountId);

        Optional<SavingAccount> deletedAccount = savingAccountRepository.findById(accountId);
        assertThat(deletedAccount).isNotPresent();
    }
}
