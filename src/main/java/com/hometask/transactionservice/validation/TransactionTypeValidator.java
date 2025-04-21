package com.hometask.transactionservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class TransactionTypeValidator implements ConstraintValidator<TransactionType, String> {
    private static final List<String> VALID_TYPES = Arrays.asList("DEPOSIT", "WITHDRAWAL", "TRANSFER");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return VALID_TYPES.contains(value.toUpperCase());
    }
} 
