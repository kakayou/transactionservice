package com.hometask.transactionservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class AccountNumberValidator implements ConstraintValidator<AccountNumber, String> {
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^\\d{5,20}$");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Allow null values (null validation is handled by @NotNull if needed)
        if (value == null) {
            return true;
        }
        
        // Check if the value matches the pattern
        return ACCOUNT_NUMBER_PATTERN.matcher(value).matches();
    }
} 
