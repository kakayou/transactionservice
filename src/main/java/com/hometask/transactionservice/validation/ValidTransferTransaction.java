package com.hometask.transactionservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidTransferTransactionValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTransferTransaction {
    String message() default "Destination account is required for TRANSFER transactions";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 
