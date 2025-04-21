package com.hometask.transactionservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class AccountNumberValidator implements ConstraintValidator<AccountNumber, String> {
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^\\d{5,20}$");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return ACCOUNT_NUMBER_PATTERN.matcher(value).matches();
    }
} 
