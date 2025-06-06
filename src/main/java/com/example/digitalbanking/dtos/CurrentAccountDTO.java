package com.example.digitalbanking.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true) // Ensure superclass fields are included in equals/hashCode
public class CurrentAccountDTO extends BankAccountDTO {
    private BigDecimal overdraft;
}
