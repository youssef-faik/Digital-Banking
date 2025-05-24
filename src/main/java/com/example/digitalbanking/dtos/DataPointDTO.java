package com.example.digitalbanking.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataPointDTO {
    private String label; // Changed from date to String to be more generic
    private Double value; // Changed from counts to a single Double value

    public DataPointDTO(String label, double value) { // Overload for double primitive
        this.label = label;
        this.value = value;
    }
}
