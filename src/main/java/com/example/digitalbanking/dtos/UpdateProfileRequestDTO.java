package com.example.digitalbanking.dtos;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateProfileRequestDTO {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
}
