package com.hometask.transactionservice.service;

import com.hometask.transactionservice.dto.TransactionRequest;
import com.hometask.transactionservice.exception.DuplicateTransactionException;
import com.hometask.transactionservice.exception.TransactionNotFoundException;
import com.hometask.transactionservice.model.Transaction;
import com.hometask.transactionservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    
    private final TransactionRepository repository;
    
    @Autowired
    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }
    
    public Transaction createTransaction(TransactionRequest request) {
        Transaction transaction = new Transaction(
                request.getAccountNumber(),
                request.getAmount(),
                request.getType(),
                request.getDescription()
        );
        transaction.setDestinationAccount(request.getDestinationAccount());
        
        // Check for duplicate transactions
        if (repository.isDuplicate(transaction)) {
            throw new DuplicateTransactionException("This appears to be a duplicate transaction for account " + 
                request.getAccountNumber() + " with amount " + request.getAmount() + " and type " + request.getType());
        }
        
        return repository.save(transaction);
    }
    
    @Cacheable(value = "transactionCache", key = "#id")
    public Transaction getTransaction(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));
    }
    
    @Cacheable(value = "allTransactionsCache")
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }
    
    public List<Transaction> getPaginatedTransactions(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one");
        }
        return repository.findAllPaginated(page, size);
    }
    
    @CacheEvict(value = {"transactionCache", "allTransactionsCache"}, key = "#id")
    public void deleteTransaction(String id) {
        if (!repository.existsById(id)) {
            throw new TransactionNotFoundException("Transaction not found with id: " + id);
        }
        repository.deleteById(id);
    }
    
    @CacheEvict(value = {"transactionCache", "allTransactionsCache"}, key = "#id")
    public Transaction updateTransaction(String id, TransactionRequest request) {
        Transaction existingTransaction = getTransaction(id);
        
        existingTransaction.setAccountNumber(request.getAccountNumber());
        existingTransaction.setAmount(request.getAmount());
        existingTransaction.setType(request.getType());
        existingTransaction.setDescription(request.getDescription());
        existingTransaction.setDestinationAccount(request.getDestinationAccount());
        
        return repository.save(existingTransaction);
    }
    
    public int getTransactionCount() {
        return repository.count();
    }
} 
