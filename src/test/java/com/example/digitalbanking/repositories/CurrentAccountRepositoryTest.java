package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.CurrentAccount;
import com.example.digitalbanking.entities.Customer;
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
public class CurrentAccountRepositoryTest {

    @Autowired
    private CurrentAccountRepository currentAccountRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(null, "Current Test Customer", "current.test@example.com");
        customer = customerRepository.save(customer);
    }

    @Test
    public void shouldSaveCurrentAccount() {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(1500.0);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setCustomer(customer);
        currentAccount.setOverdraft(300.0);

        CurrentAccount savedAccount = currentAccountRepository.save(currentAccount);
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getOverdraft()).isEqualTo(300.0);
    }

    @Test
    public void shouldFindCurrentAccountById() {
        CurrentAccount currentAccount = new CurrentAccount();
        String accountId = UUID.randomUUID().toString();
        currentAccount.setId(accountId);
        currentAccount.setBalance(2500.0);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setCustomer(customer);
        currentAccount.setOverdraft(150.0);
        currentAccountRepository.save(currentAccount);

        Optional<CurrentAccount> foundAccount = currentAccountRepository.findById(accountId);
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getId()).isEqualTo(accountId);
        assertThat(foundAccount.get().getBalance()).isEqualTo(2500.0);
    }

    @Test
    public void shouldFindAllCurrentAccounts() {
        CurrentAccount account1 = new CurrentAccount();
        account1.setId(UUID.randomUUID().toString());
        account1.setBalance(500.0);
        account1.setCreatedAt(new Date());
        account1.setCustomer(customer);
        account1.setOverdraft(100.0);
        currentAccountRepository.save(account1);

        CurrentAccount account2 = new CurrentAccount();
        account2.setId(UUID.randomUUID().toString());
        account2.setBalance(750.0);
        account2.setCreatedAt(new Date());
        account2.setCustomer(customer);
        account2.setOverdraft(250.0);
        currentAccountRepository.save(account2);

        List<CurrentAccount> accounts = currentAccountRepository.findAll();
        assertThat(accounts.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldDeleteCurrentAccount() {
        CurrentAccount currentAccount = new CurrentAccount();
        String accountId = UUID.randomUUID().toString();
        currentAccount.setId(accountId);
        currentAccount.setBalance(3000.0);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setCustomer(customer);
        currentAccount.setOverdraft(500.0);
        currentAccountRepository.save(currentAccount);

        currentAccountRepository.deleteById(accountId);

        Optional<CurrentAccount> deletedAccount = currentAccountRepository.findById(accountId);
        assertThat(deletedAccount).isNotPresent();
    }
}
