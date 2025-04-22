package com.hometask.transactionservice.service;

import com.hometask.transactionservice.dto.TransactionRequest;
import com.hometask.transactionservice.exception.DuplicateTransactionException;
import com.hometask.transactionservice.exception.TransactionNotFoundException;
import com.hometask.transactionservice.model.Transaction;
import com.hometask.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ErrorHandlingTest {

    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @BeforeEach
    public void setup() throws Exception {
        // Clear the transaction store before each test
        // This is a bit hacky but necessary since we're using an in-memory repository
        Field transactionStoreField = TransactionRepository.class.getDeclaredField("transactionStore");
        transactionStoreField.setAccessible(true);
        Map<String, Transaction> transactionStore = (Map<String, Transaction>) transactionStoreField.get(transactionRepository);
        transactionStore.clear();
    }

    private TransactionRequest createTransactionRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setType("DEPOSIT");
        request.setDescription("Test transaction");
        return request;
    }

    @Test
    public void testCreateDuplicateTransaction() {
        // Create first transaction
        TransactionRequest request1 = createTransactionRequest();
        Transaction tx1 = transactionService.createTransaction(request1);
        assertNotNull(tx1.getId());
        
        // Try to create a duplicate (same details within 60 seconds)
        TransactionRequest request2 = createTransactionRequest();
        
        // This should throw DuplicateTransactionException
        assertThrows(DuplicateTransactionException.class, () -> {
            transactionService.createTransaction(request2);
        });
    }

    @Test
    public void testDeleteNonExistentTransaction() {
        // This should throw TransactionNotFoundException
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.deleteTransaction("non-existent-id");
        });
    }
    
    @Test
    public void testUpdateNonExistentTransaction() {
        TransactionRequest request = createTransactionRequest();
        
        // This should throw TransactionNotFoundException
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.updateTransaction("non-existent-id", request);
        });
    }
    
    @Test
    public void testInvalidPaginationParameters() {
        // Negative page number
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.getPaginatedTransactions(-1, 10);
        });
        
        // Zero page size
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.getPaginatedTransactions(0, 0);
        });
    }
} 
