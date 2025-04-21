package com.hometask.transactionservice.dto;

import com.hometask.transactionservice.validation.AccountNumber;
import com.hometask.transactionservice.validation.TransactionType;
import com.hometask.transactionservice.validation.ValidTransferTransaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@ValidTransferTransaction
public class TransactionRequest {
    @NotBlank(message = "Account number is required")
    @AccountNumber
    private String accountNumber;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Transaction type is required")
    @TransactionType
    private String type;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @AccountNumber(message = "Invalid destination account format")
    private String destinationAccount;

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

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }
} 
