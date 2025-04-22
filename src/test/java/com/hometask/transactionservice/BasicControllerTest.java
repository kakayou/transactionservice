package com.hometask.transactionservice;

import com.hometask.transactionservice.dto.TransactionRequest;
import com.hometask.transactionservice.model.Transaction;
import com.hometask.transactionservice.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasicControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionService transactionService;

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(transactionService);
    }

    @Test
    public void testCreateAndGetTransaction() {
        // Create a transaction
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setType("DEPOSIT");
        request.setDescription("Test transaction");

        // Post the transaction
        ResponseEntity<Transaction> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/transactions",
                request,
                Transaction.class);

        // Verify creation was successful
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        
        Transaction created = createResponse.getBody();
        assertNotNull(created);
        assertNotNull(created.getId());
        
        // Verify GET endpoint works
        ResponseEntity<Transaction> getResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/transactions/" + created.getId(),
                Transaction.class);
                
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(created.getId(), getResponse.getBody().getId());
    }
} 