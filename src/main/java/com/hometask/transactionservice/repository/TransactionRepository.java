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
        return transactionStore.values().stream().anyMatch(existing -> 
            !existing.getId().equals(transaction.getId()) && // Different id (not the same record)
            existing.getAccountNumber().equals(transaction.getAccountNumber()) && // Same account
            existing.getAmount().compareTo(transaction.getAmount()) == 0 && // Same amount
            existing.getType().equals(transaction.getType()) && // Same type
            (existing.getDestinationAccount() == null && transaction.getDestinationAccount() == null || 
             existing.getDestinationAccount() != null && existing.getDestinationAccount().equals(transaction.getDestinationAccount())) && // Same destination (if applicable)
            ChronoUnit.SECONDS.between(existing.getTimestamp(), transaction.getTimestamp()) < 60 // Within 60 seconds
        );
    }
} 
