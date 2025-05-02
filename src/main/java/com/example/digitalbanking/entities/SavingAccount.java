package com.example.digitalbanking.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("SAVING")
public class SavingAccount extends BankAccount {
    private double interestRate;
}
