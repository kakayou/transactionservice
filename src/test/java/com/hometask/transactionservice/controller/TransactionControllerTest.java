package com.hometask.transactionservice.controller;

import com.hometask.transactionservice.dto.TransactionRequest;
import com.hometask.transactionservice.exception.TransactionNotFoundException;
import com.hometask.transactionservice.model.Transaction;
import com.hometask.transactionservice.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction transaction;
    private TransactionRequest request;

    @BeforeEach
    void setUp() {
        transaction = new Transaction(
                "test-id",
                "123456789",
                new BigDecimal("100.00"),
                "DEPOSIT",
                "Test deposit",
                LocalDateTime.now(),
                null
        );

        request = new TransactionRequest();
        request.setAccountNumber("123456789");
        request.setAmount(new BigDecimal("100.00"));
        request.setType("DEPOSIT");
        request.setDescription("Test deposit");
    }

    @Test
    void createTransaction_ShouldReturnCreatedTransaction() throws Exception {
        when(service.createTransaction(any(TransactionRequest.class))).thenReturn(transaction);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("test-id")))
                .andExpect(jsonPath("$.accountNumber", is("123456789")))
                .andExpect(jsonPath("$.type", is("DEPOSIT")));

        verify(service, times(1)).createTransaction(any(TransactionRequest.class));
    }

    @Test
    void getAllTransactions_ShouldReturnAllTransactions() throws Exception {
        List<Transaction> transactions = Arrays.asList(
                transaction,
                new Transaction("test-id-2", "987654321", new BigDecimal("200.00"), "WITHDRAWAL", "Test withdrawal", LocalDateTime.now(), null)
        );
        when(service.getPaginatedTransactions(0, 10)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("test-id")))
                .andExpect(jsonPath("$[1].id", is("test-id-2")));

        verify(service, times(1)).getPaginatedTransactions(0, 10);
    }

    @Test
    void deleteTransaction_WhenExisting_ShouldReturnNoContent() throws Exception {
        doNothing().when(service).deleteTransaction("test-id");

        mockMvc.perform(delete("/api/transactions/test-id"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deleteTransaction("test-id");
    }

    @Test
    void updateTransaction_WhenExisting_ShouldReturnUpdatedTransaction() throws Exception {
        when(service.updateTransaction(eq("test-id"), any(TransactionRequest.class))).thenReturn(transaction);

        mockMvc.perform(put("/api/transactions/test-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("test-id")));

        verify(service, times(1)).updateTransaction(eq("test-id"), any(TransactionRequest.class));
    }

    @Test
    void createTransaction_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        request.setAccountNumber("");
        request.setAmount(null);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTransaction(any(TransactionRequest.class));
    }
} 
