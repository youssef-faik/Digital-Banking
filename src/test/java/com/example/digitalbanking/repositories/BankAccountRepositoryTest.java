package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.BankAccount;
import com.example.digitalbanking.entities.CurrentAccount;
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
public class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(null, "Test Customer", "test@example.com");
        customer = customerRepository.save(customer);
    }

    @Test
    public void shouldSaveCurrentAccount() {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(1000.0);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setCustomer(customer);
        currentAccount.setOverdraft(200.0);

        BankAccount savedAccount = bankAccountRepository.save(currentAccount);
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount).isInstanceOf(CurrentAccount.class);
        assertThat(((CurrentAccount) savedAccount).getOverdraft()).isEqualTo(200.0);
    }

    @Test
    public void shouldSaveSavingAccount() {
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setBalance(5000.0);
        savingAccount.setCreatedAt(new Date());
        savingAccount.setCustomer(customer);
        savingAccount.setInterestRate(0.05);

        BankAccount savedAccount = bankAccountRepository.save(savingAccount);
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount).isInstanceOf(SavingAccount.class);
        assertThat(((SavingAccount) savedAccount).getInterestRate()).isEqualTo(0.05);
    }

    @Test
    public void shouldFindAccountById() {
        CurrentAccount currentAccount = new CurrentAccount();
        String accountId = UUID.randomUUID().toString();
        currentAccount.setId(accountId);
        currentAccount.setBalance(1500.0);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setCustomer(customer);
        currentAccount.setOverdraft(100.0);
        bankAccountRepository.save(currentAccount);

        Optional<BankAccount> foundAccount = bankAccountRepository.findById(accountId);
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getId()).isEqualTo(accountId);
        assertThat(foundAccount.get().getBalance()).isEqualTo(1500.0);
    }

    @Test
    public void shouldFindAllAccounts() {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(1000.0);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setCustomer(customer);
        currentAccount.setOverdraft(200.0);
        bankAccountRepository.save(currentAccount);

        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setBalance(5000.0);
        savingAccount.setCreatedAt(new Date());
        savingAccount.setCustomer(customer);
        savingAccount.setInterestRate(0.05);
        bankAccountRepository.save(savingAccount);

        List<BankAccount> accounts = bankAccountRepository.findAll();
        // Consider existing customer might have accounts from other tests if context is reused
        assertThat(accounts.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldDeleteAccount() {
        CurrentAccount currentAccount = new CurrentAccount();
        String accountId = UUID.randomUUID().toString();
        currentAccount.setId(accountId);
        currentAccount.setBalance(2000.0);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setCustomer(customer);
        currentAccount.setOverdraft(50.0);
        bankAccountRepository.save(currentAccount);

        bankAccountRepository.deleteById(accountId);

        Optional<BankAccount> deletedAccount = bankAccountRepository.findById(accountId);
        assertThat(deletedAccount).isNotPresent();
    }
}
