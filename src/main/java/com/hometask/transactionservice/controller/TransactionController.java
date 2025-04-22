package com.hometask.transactionservice.controller;

import com.hometask.transactionservice.dto.TransactionRequest;
import com.hometask.transactionservice.model.Transaction;
import com.hometask.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    
    private final TransactionService service;
    
    @Autowired
    public TransactionController(TransactionService service) {
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Transaction created = service.createTransaction(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Transaction> transactions = service.getPaginatedTransactions(page, size);
        return ResponseEntity.ok(transactions);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        service.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request) {
        Transaction updated = service.updateTransaction(id, request);
        return ResponseEntity.ok(updated);
    }
} 
