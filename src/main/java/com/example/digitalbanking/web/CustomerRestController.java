package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.CustomerDTO;
import com.example.digitalbanking.exceptions.CustomerNotFoundException;
import com.example.digitalbanking.services.CustomerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers") // Base path for customer-related endpoints
@AllArgsConstructor
@Slf4j
public class CustomerRestController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> listCustomers(Pageable pageable) {
        log.info("REST request to get a page of Customers");
        Page<CustomerDTO> page = customerService.listCustomers(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) throws CustomerNotFoundException {
        log.info("REST request to get Customer : {}", id);
        CustomerDTO customerDTO = customerService.getCustomer(id);
        return ResponseEntity.ok(customerDTO);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            Pageable pageable) {
        log.info("REST request to search Customers with keyword: {}", keyword);
        Page<CustomerDTO> page = customerService.searchCustomers("%" + keyword + "%", pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> saveCustomer(@RequestBody CustomerDTO customerDTO) {
        log.info("REST request to save Customer : {}", customerDTO);
        CustomerDTO savedCustomer = customerService.saveCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) throws CustomerNotFoundException {
        log.info("REST request to update Customer : {}", id);
        customerDTO.setId(id); // Ensure the ID from the path is used
        CustomerDTO updatedCustomer = customerService.updateCustomer(customerDTO);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) throws CustomerNotFoundException {
        log.info("REST request to delete Customer : {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
