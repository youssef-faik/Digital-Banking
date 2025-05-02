package com.example.digitalbanking.repositories;

import com.example.digitalbanking.entities.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void shouldSaveCustomer() {
        Customer customer = new Customer(null, "John Doe", "john.doe@example.com");
        Customer savedCustomer = customerRepository.save(customer);
        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getName()).isEqualTo("John Doe");
    }

    @Test
    public void shouldFindCustomerById() {
        Customer customer = new Customer(null, "Jane Doe", "jane.doe@example.com");
        Customer savedCustomer = customerRepository.save(customer);

        Optional<Customer> foundCustomer = customerRepository.findById(savedCustomer.getId());
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getName()).isEqualTo("Jane Doe");
    }

    @Test
    public void shouldFindAllCustomers() {
        Customer customer1 = new Customer(null, "Alice", "alice@example.com");
        Customer customer2 = new Customer(null, "Bob", "bob@example.com");
        customerRepository.save(customer1);
        customerRepository.save(customer2);

        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(2);
    }

    @Test
    public void shouldDeleteCustomer() {
        Customer customer = new Customer(null, "Charlie", "charlie@example.com");
        Customer savedCustomer = customerRepository.save(customer);
        Long customerId = savedCustomer.getId();

        customerRepository.deleteById(customerId);

        Optional<Customer> deletedCustomer = customerRepository.findById(customerId);
        assertThat(deletedCustomer).isNotPresent();
    }
}
