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

    // Method to find customers by AppUser
    Page<Customer> findByAppUser(com.example.digitalbanking.entities.AppUser appUser, Pageable pageable);

    // Method to find customers by AppUser and name containing keyword (case-insensitive)
    Page<Customer> findByAppUserAndNameContainingIgnoreCase(com.example.digitalbanking.entities.AppUser appUser, String nameKeyword, Pageable pageable);

    // Method to find customers by AppUser and email containing keyword (case-insensitive)
    Page<Customer> findByAppUserAndEmailContainingIgnoreCase(com.example.digitalbanking.entities.AppUser appUser, String emailKeyword, Pageable pageable);

    // Method to count customers by AppUser
    long countByAppUser(com.example.digitalbanking.entities.AppUser appUser);
}
