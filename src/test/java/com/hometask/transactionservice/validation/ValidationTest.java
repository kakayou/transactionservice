package com.hometask.transactionservice.validation;

import com.hometask.transactionservice.dto.TransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testValidTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setType("DEPOSIT");
        request.setDescription("Valid transaction");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testInvalidAccountNumber() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("123"); // Too short
        request.setAmount(new BigDecimal("100.00"));
        request.setType("DEPOSIT");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.accountNumber").exists());
    }

    @Test
    public void testNegativeAmount() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setAmount(new BigDecimal("-100.00")); // Negative amount
        request.setType("DEPOSIT");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    public void testInvalidTransactionType() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setType("INVALID_TYPE"); // Invalid type

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists());
    }

    @Test
    public void testTransferWithoutDestination() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setType("TRANSFER"); // Transfer without destination
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.transactionRequest").exists());
    }

    @Test
    public void testTransferToSameAccount() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setType("TRANSFER");
        request.setDestinationAccount("12345"); // Same as source
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.transactionRequest").exists());
    }
} 
