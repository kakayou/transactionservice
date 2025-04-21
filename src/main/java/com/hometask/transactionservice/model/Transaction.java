package com.hometask.transactionservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Transaction {
    private String id;
    
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotBlank(message = "Transaction type is required")
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER
    
    private String description;
    private LocalDateTime timestamp;
    
    // For transfers
    private String destinationAccount;

    public Transaction() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for creating new transactions
    public Transaction(String accountNumber, BigDecimal amount, String type, String description) {
        this();
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    // Full constructor
    public Transaction(String id, String accountNumber, BigDecimal amount, String type, 
                      String description, LocalDateTime timestamp, String destinationAccount) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
        this.destinationAccount = destinationAccount;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 
