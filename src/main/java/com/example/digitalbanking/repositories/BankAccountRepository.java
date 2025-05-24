package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    // Add method to check if any bank account exists for a given customer ID
    boolean existsByCustomerId(Long customerId);

    // Method to find bank accounts by Customer's AppUser
    org.springframework.data.domain.Page<BankAccount> findByCustomerAppUser(com.example.digitalbanking.entities.AppUser appUser, org.springframework.data.domain.Pageable pageable);
}
