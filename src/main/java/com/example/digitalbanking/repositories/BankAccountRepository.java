package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    // Add method to check if any bank account exists for a given customer ID
    boolean existsByCustomerId(Long customerId);
}
