package com.hometask.transactionservice.validation;

import com.hometask.transactionservice.dto.TransactionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTransferTransactionValidator implements ConstraintValidator<ValidTransferTransaction, TransactionRequest> {

    @Override
    public boolean isValid(TransactionRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        
        if ("TRANSFER".equalsIgnoreCase(request.getType())) {
            // For TRANSFER type, destination account must be non-null and different from source
            return request.getDestinationAccount() != null && 
                   !request.getDestinationAccount().isEmpty() &&
                   !request.getDestinationAccount().equals(request.getAccountNumber());
        }
        
        return true;
    }
} 
