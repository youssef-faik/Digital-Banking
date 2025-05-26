package com.example.digitalbanking.services;

import com.example.digitalbanking.dtos.CustomerDTO;
import com.example.digitalbanking.entities.AppUser;
import com.example.digitalbanking.entities.Customer;
import com.example.digitalbanking.exceptions.CustomerDeletionException;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import com.example.digitalbanking.mappers.BankAccountMapper;
import com.example.digitalbanking.repositories.AppUserRepository;
import com.example.digitalbanking.repositories.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final BankAccountMapper dtoMapper;
    private final AppUserRepository appUserRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               BankAccountMapper dtoMapper,
                               AppUserRepository appUserRepository) {
        this.customerRepository = customerRepository;
        this.dtoMapper = dtoMapper;
        this.appUserRepository = appUserRepository;
    }

    private AppUser getCurrentAuthenticatedAppUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + username));
    }    private void checkCustomerOwnership(Customer customer, AppUser appUser) {
        // Allow ADMIN users to access any customer
        boolean isAdmin = appUser.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleName()));
        
        if (isAdmin) {
            log.info("Admin user {} accessing customer {}", appUser.getUsername(), customer.getId());
            return;
        }
        
        // For non-admin users, check ownership
        if (customer.getAppUser() == null || !customer.getAppUser().getUserId().equals(appUser.getUserId())) {
            log.warn("User {} attempted to access or modify customer {} owned by a different user or with no owner.", appUser.getUsername(), customer.getId());
            throw new AccessDeniedException("You do not have permission to access or modify this customer.");
        }
    }

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        customer.setId(null); // Ensure ID is null for new customers
        customer.setAppUser(currentUser); // Associate the customer with the authenticated user
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public Page<CustomerDTO> listCustomers(Pageable pageable) {
        // This method lists ALL customers. For user-specific, see searchCustomers or a new method.
        // Consider adding role check for admin if this is sensitive.
        log.info("Listing all customers (admin/general access)");
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(dtoMapper::fromCustomer);
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        log.info("Getting Customer with ID: {}", customerId);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found with ID: " + customerId));
        checkCustomerOwnership(customer, currentUser);
        return dtoMapper.fromCustomer(customer);
    }    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException {
        log.info("Updating Customer: {}", customerDTO.getId());
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer existingCustomer = customerRepository.findById(customerDTO.getId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found with ID: " + customerDTO.getId()));
        checkCustomerOwnership(existingCustomer, currentUser);

        // Update only the modifiable fields on the existing entity to avoid foreign key issues
        existingCustomer.setName(customerDTO.getName());
        existingCustomer.setEmail(customerDTO.getEmail());
        // Don't modify appUser or bankAccounts relationships

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return dtoMapper.fromCustomer(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) throws CustomerNotFoundException {
        log.info("Deleting Customer with ID: {}", customerId);
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found with ID: " + customerId));
        checkCustomerOwnership(customer, currentUser);

        if (customer.getBankAccounts() != null && !customer.getBankAccounts().isEmpty()) {
            // Throw the specific exception
            throw new CustomerDeletionException("Cannot delete customer with ID: " + customerId + " because they have associated bank accounts.");
        }

        customerRepository.delete(customer);
        log.info("Customer deleted successfully: {}", customerId);
    }

    @Override
    public Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable) {
        AppUser currentUser = getCurrentAuthenticatedAppUser();
        log.info("Searching customers for user {} with keyword: {}", currentUser.getUsername(), keyword);
        Page<Customer> customersPage = customerRepository.findByAppUserAndNameContainingIgnoreCase(currentUser, keyword, pageable);
        if (customersPage == null || !customersPage.hasContent()){
             customersPage = customerRepository.findByAppUserAndEmailContainingIgnoreCase(currentUser, keyword, pageable);
        }
        // If still no results, return empty page
        if (customersPage == null || !customersPage.hasContent()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return customersPage.map(dtoMapper::fromCustomer);
    }
}
