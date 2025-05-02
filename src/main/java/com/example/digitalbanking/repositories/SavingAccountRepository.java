package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, String> {
}
