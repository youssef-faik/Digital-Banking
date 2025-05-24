package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.CustomerDTO;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import com.example.digitalbanking.services.CustomerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> listCustomers(Pageable pageable) {
        log.info("REST request to get a page of Customers");
        Page<CustomerDTO> page = customerService.listCustomers(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        log.info("REST request to get Customer : {}", id);
        try {
            CustomerDTO customerDTO = customerService.getCustomer(id);
            return ResponseEntity.ok(customerDTO);
        } catch (CustomerNotFoundException e) {
            log.warn("Customer not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        log.info("REST request to create Customer : {}", customerDTO);
        CustomerDTO savedCustomer = customerService.saveCustomer(customerDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedCustomer.getId()).toUri();
        return ResponseEntity.created(location).body(savedCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
        log.info("REST request to update Customer : {} with data : {}", id, customerDTO);
        try {
            if (customerDTO.getId() == null) {
                customerDTO.setId(id);
            } else if (!customerDTO.getId().equals(id)) {
                log.warn("ID in path ({}) does not match ID in body ({})", id, customerDTO.getId());
                return ResponseEntity.badRequest().build();
            }
            CustomerDTO updatedCustomer = customerService.updateCustomer(customerDTO);
            return ResponseEntity.ok(updatedCustomer);
        } catch (CustomerNotFoundException e) {
            log.warn("Customer not found with ID: {} for update", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("REST request to delete Customer : {}", id);
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (CustomerNotFoundException e) {
            log.warn("Customer not found with ID: {} for deletion", id);
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            log.error("Error deleting customer with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(@RequestParam(name = "keyword", defaultValue = "") String keyword, Pageable pageable) {
        log.info("REST request to search Customers with keyword: {}", keyword);
        Page<CustomerDTO> page = customerService.searchCustomers(keyword, pageable);
        return ResponseEntity.ok(page);
    }
}
