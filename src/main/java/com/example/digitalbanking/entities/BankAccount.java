package com.example.digitalbanking.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer", "accountOperations"})
@EqualsAndHashCode(exclude = {"customer", "accountOperations"})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ACCOUNT_TYPE", length = 10)
public abstract class BankAccount {
    @Id
    private String id;
    private BigDecimal balance;
    private Instant createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
    
    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE; // Default to ACTIVE

    @OneToMany(mappedBy = "bankAccount", fetch = FetchType.LAZY)
    private List<AccountOperation> accountOperations;
}
