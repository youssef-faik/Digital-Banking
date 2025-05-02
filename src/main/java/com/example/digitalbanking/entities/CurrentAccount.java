package com.example.digitalbanking.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("CURRENT")
public class CurrentAccount extends BankAccount {
    private double overdraft;
}
