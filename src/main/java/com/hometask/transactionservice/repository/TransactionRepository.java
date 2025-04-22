package com.hometask.transactionservice.repository;

import com.hometask.transactionservice.model.Transaction;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {
    
    private final Map<String, Transaction> transactionStore = new ConcurrentHashMap<>();
    
    public Transaction save(Transaction transaction) {
        transactionStore.put(transaction.getId(), transaction);
        return transaction;
    }
    
    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(transactionStore.get(id));
    }
    
    public List<Transaction> findAll() {
        return new ArrayList<>(transactionStore.values());
    }
    
    public List<Transaction> findAllPaginated(int page, int size) {
        return transactionStore.values().stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }
    
    public void deleteById(String id) {
        transactionStore.remove(id);
    }
    
    public int count() {
        return transactionStore.size();
    }
    
    public boolean existsById(String id) {
        return transactionStore.containsKey(id);
    }
    
    public boolean isDuplicate(Transaction transaction) {
        // Generate a simple "signature" for the transaction that combines account, amount, and type
        String transactionSignature = transaction.getAccountNumber() + "_" + 
                                     transaction.getAmount() + "_" + 
                                     transaction.getType() + "_" + 
                                     transaction.getDestinationAccount();
        
        // Check if we have a transaction with the same signature in the last 60 seconds
        return transactionStore.values().stream().anyMatch(existing -> {
            // Different id (not updating the same record)
            if (existing.getId().equals(transaction.getId())) {
                return false;
            }
            
            // Generate signature for existing transaction
            String existingSignature = existing.getAccountNumber() + "_" + 
                                      existing.getAmount() + "_" + 
                                      existing.getType() + "_" + 
                                      existing.getDestinationAccount();
            
            // Check if signatures match and if transaction is within the last 60 seconds
            return existingSignature.equals(transactionSignature) && 
                   Math.abs(ChronoUnit.SECONDS.between(existing.getTimestamp(), transaction.getTimestamp())) < 60;
        });
    }
} 
