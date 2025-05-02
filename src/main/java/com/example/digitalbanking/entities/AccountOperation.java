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
@ToString(exclude = "bankAccount")
@EqualsAndHashCode(exclude = "bankAccount")
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
}

