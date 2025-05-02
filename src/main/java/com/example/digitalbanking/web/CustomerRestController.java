package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.CustomerDTO;
import com.example.digitalbanking.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject; // Import ParameterObject
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers") // Base path for customer-related endpoints
@AllArgsConstructor
@Slf4j
@Tag(name = "Customer API", description = "Endpoints for managing bank customers")
public class CustomerRestController {

    private final CustomerService customerService;

    @Operation(summary = "List all customers", description = "Retrieves a paginated list of all customers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))) // Note: Schema might need refinement for Page<CustomerDTO>
    })
    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> listCustomers(
            @ParameterObject Pageable pageable) { // Use @ParameterObject for Pageable
        log.info("REST request to get a page of Customers");
        Page<CustomerDTO> page = customerService.listCustomers(pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves a specific customer by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customer",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(
            @Parameter(description = "ID of the customer to retrieve", required = true, example = "1") @PathVariable Long id) {
        log.info("REST request to get Customer : {}", id);
        CustomerDTO customerDTO = customerService.getCustomer(id);
        return ResponseEntity.ok(customerDTO);
    }

    @Operation(summary = "Search customers", description = "Searches for customers by a keyword in their name (case-insensitive). Returns a paginated list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))) // Note: Schema might need refinement for Page<CustomerDTO>
    })
    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
            @Parameter(description = "Keyword to search for in customer names", example = "John") @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @ParameterObject Pageable pageable) { // Use @ParameterObject for Pageable
        log.info("REST request to search Customers with keyword: {}", keyword);
        Page<CustomerDTO> page = customerService.searchCustomers("%" + keyword + "%", pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Create a new customer", description = "Adds a new customer to the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<CustomerDTO> saveCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Customer object to be created", required = true,
                    content = @Content(schema = @Schema(implementation = CustomerDTO.class),
                                     examples = @ExampleObject(value = "{\"name\": \"Jane Doe\", \"email\": \"jane.doe@example.com\"}")))
            @RequestBody CustomerDTO customerDTO) {
        log.info("REST request to save Customer : {}", customerDTO);
        CustomerDTO savedCustomer = customerService.saveCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

    @Operation(summary = "Update an existing customer", description = "Updates the details of an existing customer by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @Parameter(description = "ID of the customer to update", required = true, example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated customer object", required = true,
                    content = @Content(schema = @Schema(implementation = CustomerDTO.class),
                                     examples = @ExampleObject(value = "{\"name\": \"Jane D. Smith\", \"email\": \"jane.smith@example.com\"}")))
            @RequestBody CustomerDTO customerDTO) {
        log.info("REST request to update Customer : {}", id);
        customerDTO.setId(id); // Ensure the ID from the path is used
        CustomerDTO updatedCustomer = customerService.updateCustomer(customerDTO);
        return ResponseEntity.ok(updatedCustomer);
    }

    @Operation(summary = "Delete a customer", description = "Deletes a customer by their ID. Fails if the customer has associated bank accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Cannot delete customer with accounts", // Assuming CustomerDeletionException maps to 400
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "ID of the customer to delete", required = true, example = "1") @PathVariable Long id) {
        log.info("REST request to delete Customer : {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
