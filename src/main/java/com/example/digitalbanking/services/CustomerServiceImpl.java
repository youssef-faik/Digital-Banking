package com.example.digitalbanking.services;

import com.example.digitalbanking.dtos.CustomerDTO;
import com.example.digitalbanking.entities.Customer;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import com.example.digitalbanking.mappers.BankAccountMapper;
import com.example.digitalbanking.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final BankAccountMapper dtoMapper; // Assuming a mapper exists

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public Page<CustomerDTO> listCustomers(Pageable pageable) {
        log.info("Listing customers");
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(dtoMapper::fromCustomer);
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        log.info("Getting customer by ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException {
        log.info("Updating Customer: {}", customerDTO.getId());
        // Ensure customer exists before attempting update
        getCustomer(customerDTO.getId()); 
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer updatedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) throws CustomerNotFoundException {
        log.info("Deleting Customer: {}", customerId);
        // Ensure customer exists before attempting delete
        getCustomer(customerId);
        customerRepository.deleteById(customerId);
    }

    @Override
    public Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable) {
        log.info("Searching customers with keyword: {}", keyword);
        Page<Customer> customers = customerRepository.searchCustomerByName(keyword, pageable);
        return customers.map(dtoMapper::fromCustomer);
    }
}
