package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.AccountOperation;
import com.example.digitalbanking.entities.AppUser; // Added import
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {
    // Add method to find operations by account ID with pagination, ordered by date descending
    Page<AccountOperation> findByBankAccountIdOrderByOperationDateDesc(String accountId, Pageable pageable);
    long countByAppUser(AppUser appUser); // Added for dashboard statistics
}
