package com.hometask.transactionservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TransactionTypeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionType {
    String message() default "Invalid transaction type. Allowed types are: DEPOSIT, WITHDRAWAL, TRANSFER";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 
