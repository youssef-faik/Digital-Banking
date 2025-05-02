package com.example.digitalbanking.services;

import com.example.digitalbanking.dtos.CustomerDTO;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    Page<CustomerDTO> listCustomers(Pageable pageable);
    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;
    CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException;
    void deleteCustomer(Long customerId) throws CustomerNotFoundException;
    Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable);
}
