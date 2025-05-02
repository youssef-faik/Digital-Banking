package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Method to search customers by name containing a keyword (case-insensitive)
    @Query("select c from Customer c where lower(c.name) like lower(:kw)")
    Page<Customer> searchCustomerByName(@Param("kw") String keyword, Pageable pageable);

    // Optional: If you need a simple list search without pagination
    // List<Customer> findByNameContainingIgnoreCase(String keyword);
}
