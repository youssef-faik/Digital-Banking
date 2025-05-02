package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.CurrentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrentAccountRepository extends JpaRepository<CurrentAccount, String> {
}
