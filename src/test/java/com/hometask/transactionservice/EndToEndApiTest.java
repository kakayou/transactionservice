package com.hometask.transactionservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.transactionservice.dto.TransactionRequest;
import com.hometask.transactionservice.model.Transaction;
import com.hometask.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EndToEndApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository repository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String baseUrl;
    
    @BeforeEach
    public void setup() throws Exception {
        // Clear the repository before each test
        Field transactionStoreField = TransactionRepository.class.getDeclaredField("transactionStore");
        transactionStoreField.setAccessible(true);
        Map<String, Transaction> transactionStore = (Map<String, Transaction>) transactionStoreField.get(repository);
        transactionStore.clear();
        
        baseUrl = "http://localhost:" + port + "/api/transactions";
    }
    
    private TransactionRequest createSampleRequest(String accountNumber, BigDecimal amount, String type) {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber(accountNumber);
        request.setAmount(amount);
        request.setType(type);
        request.setDescription("Test transaction");
        return request;
    }
    
    @Test
    @Order(1)
    public void testCreateTransaction() {
        // Create a transaction request
        TransactionRequest request = createSampleRequest("12345", new BigDecimal("100.00"), "DEPOSIT");
        
        // Send POST request
        ResponseEntity<Transaction> response = restTemplate.postForEntity(
                baseUrl, request, Transaction.class);
        
        // Verify response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Transaction created = response.getBody();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("12345", created.getAccountNumber());
        assertEquals(0, new BigDecimal("100.00").compareTo(created.getAmount()));
        assertEquals("DEPOSIT", created.getType());
    }
    
    @Test
    @Order(2)
    public void testCreateInvalidTransaction() {
        // Create an invalid transaction (missing required fields)
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("123"); // Invalid account number (too short)
        request.setAmount(new BigDecimal("100.00"));
        request.setType("DEPOSIT");
        
        // Send POST request
        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl, request, Object.class);
        
        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    @Order(3)
    public void testGetAllTransactions() {
        // Create multiple transactions with different account numbers to avoid duplicate detection
        restTemplate.postForEntity(baseUrl, 
                createSampleRequest("11111", new BigDecimal("100.00"), "DEPOSIT"), 
                Transaction.class);
        restTemplate.postForEntity(baseUrl, 
                createSampleRequest("22222", new BigDecimal("200.00"), "WITHDRAWAL"), 
                Transaction.class);
        
        // Get all transactions
        ResponseEntity<List<Transaction>> response = restTemplate.exchange(
                baseUrl, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<Transaction>>(){});
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Transaction> transactions = response.getBody();
        assertNotNull(transactions);
        assertTrue(transactions.size() >= 2);
    }
    
    @Test
    @Order(4)
    public void testGetTransactionsWithPagination() {
        // First clear the repository to ensure we have exactly the number of transactions we create
        try {
            Field transactionStoreField = TransactionRepository.class.getDeclaredField("transactionStore");
            transactionStoreField.setAccessible(true);
            Map<String, Transaction> transactionStore = (Map<String, Transaction>) transactionStoreField.get(repository);
            transactionStore.clear();
        } catch (Exception e) {
            fail("Failed to clear repository: " + e.getMessage());
        }

        // Create transactions with unique account numbers to avoid duplicate detection
        // Create exactly 5 transactions for predictable pagination results
        for (int i = 1; i <= 5; i++) {
            String uniqueAccountNumber = String.format("%d%d", i, System.currentTimeMillis() % 10000);

            // Add a small delay to ensure unique timestamps
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            TransactionRequest request = createSampleRequest(
                    uniqueAccountNumber,
                    new BigDecimal(100 + i),
                    "DEPOSIT"
            );

            ResponseEntity<Transaction> response = restTemplate.postForEntity(
                    baseUrl, request, Transaction.class);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        // Verify we have 5 transactions total
        ResponseEntity<List<Transaction>> allResponse = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Transaction>>(){});

        assertEquals(HttpStatus.OK, allResponse.getStatusCode());
        assertEquals(5, allResponse.getBody().size(), "Should have exactly 5 transactions in total");

        // Get first page (2 items)
        String firstPageUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("page", 0)
                .queryParam("size", 2)
                .toUriString();

        ResponseEntity<List<Transaction>> firstPageResponse = restTemplate.exchange(
                firstPageUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Transaction>>(){});

        // Verify first page response
        assertEquals(HttpStatus.OK, firstPageResponse.getStatusCode());
        List<Transaction> firstPageTransactions = firstPageResponse.getBody();
        assertNotNull(firstPageTransactions);
        assertEquals(2, firstPageTransactions.size(), "First page should contain exactly 2 items");

        // Get second page (2 items)
        String secondPageUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("page", 1)
                .queryParam("size", 2)
                .toUriString();

        ResponseEntity<List<Transaction>> secondPageResponse = restTemplate.exchange(
                secondPageUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Transaction>>(){});

        // Verify second page response
        assertEquals(HttpStatus.OK, secondPageResponse.getStatusCode());
        List<Transaction> secondPageTransactions = secondPageResponse.getBody();
        assertNotNull(secondPageTransactions);
        assertEquals(2, secondPageTransactions.size(), "Second page should contain exactly 2 items");

        // Verify different transactions on different pages
        assertNotEquals(
                firstPageTransactions.get(0).getId(),
                secondPageTransactions.get(0).getId(),
                "Transactions on different pages should be different"
        );
    }

    @Test
    @Order(5)
    public void testUpdateTransaction() {
        // First clear the repository to prevent any issues with existing transactions
        try {
            Field transactionStoreField = TransactionRepository.class.getDeclaredField("transactionStore");
            transactionStoreField.setAccessible(true);
            Map<String, Transaction> transactionStore = (Map<String, Transaction>) transactionStoreField.get(repository);
            transactionStore.clear();
        } catch (Exception e) {
            fail("Failed to clear repository: " + e.getMessage());
        }
        
        // First create a transaction with a unique account number
        String uniqueAccountNumber = "13579";
        TransactionRequest createRequest = createSampleRequest(
                uniqueAccountNumber, 
                new BigDecimal("100.00"), 
                "DEPOSIT"
        );
        
        ResponseEntity<Transaction> createResponse = restTemplate.postForEntity(
                baseUrl, createRequest, Transaction.class);
        
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Transaction created = createResponse.getBody();
        assertNotNull(created);
        
        // Wait to ensure we're not caught by duplicate detection (which checks within 60 seconds)
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Update the transaction with a different account number and amount

        TransactionRequest updateRequest = createSampleRequest(
                uniqueAccountNumber,
                new BigDecimal("200.00"), 
                "WITHDRAWAL"
        );
        updateRequest.setDescription("Updated description");
        
        HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(updateRequest);
        ResponseEntity<Transaction> updateResponse = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.PUT,
                requestEntity,
                Transaction.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        Transaction updated = updateResponse.getBody();
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId()); // ID should remain the same
        assertEquals(0, new BigDecimal("200.00").compareTo(updated.getAmount())); // Amount should be updated
        assertEquals("WITHDRAWAL", updated.getType()); // Type should be updated
        assertEquals("Updated description", updated.getDescription()); // Description should be updated
    }
    
    @Test
    @Order(6)
    public void testUpdateNonExistentTransaction() {
        // Try to update a non-existent transaction
        TransactionRequest updateRequest = createSampleRequest("12345", new BigDecimal("100.00"), "DEPOSIT");
        HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(updateRequest);
        
        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/non-existent-id",
                HttpMethod.PUT,
                requestEntity,
                Object.class);
        
        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    @Order(7)
    public void testDeleteTransaction() {
        // First create a transaction
        TransactionRequest request = createSampleRequest("12345", new BigDecimal("100.00"), "DEPOSIT");
        ResponseEntity<Transaction> createResponse = restTemplate.postForEntity(
                baseUrl, request, Transaction.class);
        Transaction created = createResponse.getBody();
        
        // Delete the transaction
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);
        
        // Verify response
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());


    }
    
    @Test
    @Order(8)
    public void testDeleteNonExistentTransaction() {
        // Try to delete a non-existent transaction
        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/non-existent-id",
                HttpMethod.DELETE,
                null,
                Object.class);
        
        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    @Order(9)
    public void testTransferTransaction() {
        // First clear the repository to prevent any issues with existing transactions
        try {
            Field transactionStoreField = TransactionRepository.class.getDeclaredField("transactionStore");
            transactionStoreField.setAccessible(true);
            Map<String, Transaction> transactionStore = (Map<String, Transaction>) transactionStoreField.get(repository);
            transactionStore.clear();
        } catch (Exception e) {
            fail("Failed to clear repository: " + e.getMessage());
        }
        
        // Create a transfer transaction with unique account numbers
        String sourceAccount = "1" + System.currentTimeMillis();
        String destAccount = "2" + System.currentTimeMillis();
        
        TransactionRequest request = createSampleRequest(sourceAccount, new BigDecimal("100.00"), "TRANSFER");
        request.setDestinationAccount(destAccount); // Required for transfer
        
        // Send POST request
        ResponseEntity<Transaction> response = restTemplate.postForEntity(
                baseUrl, request, Transaction.class);
        
        // Verify response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Transaction created = response.getBody();
        assertNotNull(created);
        assertEquals("TRANSFER", created.getType());
        assertEquals(sourceAccount, created.getAccountNumber());
        assertEquals(destAccount, created.getDestinationAccount());
    }
    
    @Test
    @Order(10)
    public void testInvalidTransferTransaction() {
        // First clear the repository to prevent any issues with existing transactions
        try {
            Field transactionStoreField = TransactionRepository.class.getDeclaredField("transactionStore");
            transactionStoreField.setAccessible(true);
            Map<String, Transaction> transactionStore = (Map<String, Transaction>) transactionStoreField.get(repository);
            transactionStore.clear();
        } catch (Exception e) {
            fail("Failed to clear repository: " + e.getMessage());
        }
        
        // Create an invalid transfer (missing destination) with unique account number
        String sourceAccount = "invalid_source_" + System.currentTimeMillis();
        TransactionRequest request = createSampleRequest(sourceAccount, new BigDecimal("100.00"), "TRANSFER");
        // Destination account is missing
        
        // Send POST request
        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl, request, Object.class);
        
        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
} 