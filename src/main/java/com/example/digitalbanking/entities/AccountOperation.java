package com.example.digitalbanking.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"bankAccount", "appUser"})
@EqualsAndHashCode(exclude = {"bankAccount", "appUser"})
public class AccountOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant operationDate;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private OperationType type;
    @ManyToOne(fetch = FetchType.LAZY)
    private BankAccount bankAccount;
    private String description;
    @ManyToOne // Added AppUser association
    @JoinColumn(name = "user_id") // Explicitly name the foreign key column
    private AppUser appUser; // User who performed or is related to the operation
}

