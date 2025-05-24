package com.example.digitalbanking.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    @NotEmpty(message = "Customer name cannot be empty")
    private String name;
    @NotEmpty(message = "Customer email cannot be empty")
    @Email(message = "Email format is not valid")
    private String email;
    private String createdByUsername; // Add username of the creator/owner
}
