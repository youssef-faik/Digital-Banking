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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.net.URI;

@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "API for customer CRUD operations")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(
        summary = "Get all customers", 
        description = "Retrieves a paginated list of all customers"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved customer list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> listCustomers(
        @Parameter(description = "Pagination information")
        Pageable pageable
    ) {
        log.info("REST request to get a page of Customers");
        Page<CustomerDTO> page = customerService.listCustomers(pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(
        summary = "Get customer by ID", 
        description = "Retrieves a specific customer by their ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved customer",
            content = @Content(schema = @Schema(implementation = CustomerDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Customer not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(
        @Parameter(description = "ID of the customer to retrieve", required = true)
        @PathVariable Long id
    ) {
        log.info("REST request to get Customer : {}", id);
        try {
            CustomerDTO customerDTO = customerService.getCustomer(id);
            return ResponseEntity.ok(customerDTO);
        } catch (CustomerNotFoundException e) {
            log.warn("Customer not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Create a new customer", 
        description = "Creates a new customer with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Customer successfully created",
            content = @Content(schema = @Schema(implementation = CustomerDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Invalid customer data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Customer details to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CustomerDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"firstName\": \"John\",\n" +
                            "  \"lastName\": \"Doe\",\n" +
                            "  \"email\": \"john.doe@example.com\",\n" +
                            "  \"phoneNumber\": \"+1234567890\"\n" +
                            "}"
                )
            )
        )
        @Valid @RequestBody CustomerDTO customerDTO
    ) {
        log.info("REST request to create Customer : {}", customerDTO);
        CustomerDTO savedCustomer = customerService.saveCustomer(customerDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedCustomer.getId()).toUri();
        return ResponseEntity.created(location).body(savedCustomer);
    }

    @Operation(
        summary = "Update an existing customer", 
        description = "Updates an existing customer's details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Customer successfully updated",
            content = @Content(schema = @Schema(implementation = CustomerDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Invalid customer data or ID mismatch",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Customer not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(
        @Parameter(description = "ID of the customer to update", required = true)
        @PathVariable Long id,
        
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated customer details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CustomerDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"id\": 1,\n" +
                            "  \"firstName\": \"John\",\n" +
                            "  \"lastName\": \"Doe Updated\",\n" +
                            "  \"email\": \"john.updated@example.com\",\n" +
                            "  \"phoneNumber\": \"+1987654321\"\n" +
                            "}"
                )
            )
        )
        @Valid @RequestBody CustomerDTO customerDTO
    ) {
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

    @Operation(
        summary = "Delete a customer", 
        description = "Deletes a customer by their ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Customer successfully deleted",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Customer not found with the given ID",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Conflict - Cannot delete customer with linked accounts or operations",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
        @Parameter(description = "ID of the customer to delete", required = true)
        @PathVariable Long id
    ) {
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

    @Operation(
        summary = "Search customers", 
        description = "Searches for customers based on a keyword (searches in first name, last name, and email)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved search results",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - JWT token is missing or invalid",
            content = @Content
        )
    })
    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
        @Parameter(description = "Keyword to search for in customer details")
        @RequestParam(name = "keyword", defaultValue = "") String keyword,
        
        @Parameter(description = "Pagination information")
        Pageable pageable
    ) {
        log.info("REST request to search Customers with keyword: {}", keyword);
        Page<CustomerDTO> page = customerService.searchCustomers(keyword, pageable);
        return ResponseEntity.ok(page);
    }
}
