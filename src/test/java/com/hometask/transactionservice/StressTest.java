package com.hometask.transactionservice;

import com.hometask.transactionservice.dto.TransactionRequest;
import com.hometask.transactionservice.model.Transaction;
import com.hometask.transactionservice.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StressTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionService transactionService;

    private final Random random = new Random();
    private final String[] TRANSACTION_TYPES = {"DEPOSIT", "WITHDRAWAL", "TRANSFER"};
    private final String[] ACCOUNTS = {"1001", "1002", "1003", "1004", "1005"};

    @Test
    public void concurrentCreationStressTest() throws Exception {
        int numThreads = 50;
        int requestsPerThread = 100;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // Submit concurrent requests
        for (int i = 0; i < numThreads; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        TransactionRequest request = createRandomTransaction();
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<TransactionRequest> entity = new HttpEntity<>(request, headers);
                        
                        ResponseEntity<Transaction> response = restTemplate.postForEntity(
                                "http://localhost:" + port + "/api/transactions", 
                                entity, 
                                Transaction.class);
                        
                        if (response.getStatusCode().is2xxSuccessful()) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(120, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        double totalTimeInSeconds = (endTime - startTime) / 1000.0;
        int totalRequests = numThreads * requestsPerThread;
        double throughput = totalRequests / totalTimeInSeconds;
        
        // Verify results
        int totalTransactions = transactionService.getTransactionCount();
        System.out.println("Stress Test Results:");
        System.out.println("Total time: " + totalTimeInSeconds + " seconds");
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        System.out.println("Throughput: " + throughput + " requests/second");
        System.out.println("Total transactions in repository: " + totalTransactions);
        
        // Assertions to verify test success
        assertEquals(totalRequests, successCount.get(), 
                "All requests should be successful");
        assertEquals(0, errorCount.get(), 
                "There should be no errors");
        assertTrue(throughput > 50, 
                "Throughput should be at least 50 transactions per second");
    }
    
    @Test
    public void mixedOperationsStressTest() throws Exception {
        // First create some transactions
        int initialBatch = 100;
        List<String> transactionIds = new ArrayList<>();
        
        for (int i = 0; i < initialBatch; i++) {
            TransactionRequest request = createRandomTransaction();
            ResponseEntity<Transaction> response = restTemplate.postForEntity(
                    "http://localhost:" + port + "/api/transactions", 
                    request, 
                    Transaction.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                transactionIds.add(response.getBody().getId());
            }
        }
        
        // Now perform mixed operations concurrently
        int numThreads = 30;
        int operationsPerThread = 50;
        AtomicInteger successCount = new AtomicInteger(0);
        
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numThreads; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    try {
                        // Randomly select operation: 0=create, 1=update, 2=delete, 3=get all
                        int operation = random.nextInt(4);
                        
                        if (operation == 0) {
                            // Create operation
                            TransactionRequest request = createRandomTransaction();
                            ResponseEntity<Transaction> response = restTemplate.postForEntity(
                                    "http://localhost:" + port + "/api/transactions", 
                                    request, 
                                    Transaction.class);
                            
                            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                synchronized (transactionIds) {
                                    transactionIds.add(response.getBody().getId());
                                }
                                successCount.incrementAndGet();
                            }
                        } else if (operation == 1 && !transactionIds.isEmpty()) {
                            // Update operation
                            String idToUpdate;
                            synchronized (transactionIds) {
                                idToUpdate = transactionIds.get(random.nextInt(transactionIds.size()));
                            }
                            
                            TransactionRequest request = createRandomTransaction();
                            restTemplate.put(
                                    "http://localhost:" + port + "/api/transactions/" + idToUpdate, 
                                    request);
                            successCount.incrementAndGet();
                        } else if (operation == 2 && !transactionIds.isEmpty()) {
                            // Delete operation
                            String idToDelete;
                            synchronized (transactionIds) {
                                int index = random.nextInt(transactionIds.size());
                                idToDelete = transactionIds.get(index);
                                transactionIds.remove(index);
                            }
                            
                            restTemplate.delete(
                                    "http://localhost:" + port + "/api/transactions/" + idToDelete);
                            successCount.incrementAndGet();
                        } else {
                            // Get all operation with random pagination
                            int page = random.nextInt(5);
                            int size = 10 + random.nextInt(20);
                            
                            restTemplate.getForEntity(
                                    "http://localhost:" + port + "/api/transactions?page=" + page + "&size=" + size,
                                    Object.class);
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Ignore errors for this test
                    }
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(180, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        double totalTimeInSeconds = (endTime - startTime) / 1000.0;
        double throughput = successCount.get() / totalTimeInSeconds;
        
        // Print results
        System.out.println("Mixed Operations Stress Test Results:");
        System.out.println("Total time: " + totalTimeInSeconds + " seconds");
        System.out.println("Successful operations: " + successCount.get());
        System.out.println("Throughput: " + throughput + " operations/second");
        
        // Assertion to verify test success
        assertTrue(throughput > 30, 
                "Throughput should be at least 30 operations per second for mixed workload");
    }
    
    private TransactionRequest createRandomTransaction() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber(ACCOUNTS[random.nextInt(ACCOUNTS.length)]);
        request.setAmount(BigDecimal.valueOf(10 + random.nextInt(990)));
        request.setType(TRANSACTION_TYPES[random.nextInt(TRANSACTION_TYPES.length)]);
        request.setDescription("Stress test transaction");
        
        if (request.getType().equals("TRANSFER")) {
            // For transfers, set a destination account
            String destinationAccount;
            do {
                destinationAccount = ACCOUNTS[random.nextInt(ACCOUNTS.length)];
            } while (destinationAccount.equals(request.getAccountNumber()));
            
            request.setDestinationAccount(destinationAccount);
        }
        
        return request;
    }
} 
