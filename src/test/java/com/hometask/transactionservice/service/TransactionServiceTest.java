package com.hometask.transactionservice.service;

import com.hometask.transactionservice.dto.TransactionRequest;
import com.hometask.transactionservice.exception.TransactionNotFoundException;
import com.hometask.transactionservice.model.Transaction;
import com.hometask.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionService service;

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
    void createTransaction_ShouldReturnCreatedTransaction() {
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.createTransaction(request);

        assertNotNull(result);
        assertEquals(request.getAccountNumber(), result.getAccountNumber());
        assertEquals(request.getAmount(), result.getAmount());
        assertEquals(request.getType(), result.getType());
        assertEquals(request.getDescription(), result.getDescription());
        verify(repository, times(1)).save(any(Transaction.class));
    }

    @Test
    void getTransaction_WhenExisting_ShouldReturnTransaction() {
        when(repository.findById("test-id")).thenReturn(Optional.of(transaction));

        Transaction result = service.getTransaction("test-id");

        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        verify(repository, times(1)).findById("test-id");
    }

    @Test
    void getTransaction_WhenNotExisting_ShouldThrowException() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> {
            service.getTransaction("non-existent");
        });
        verify(repository, times(1)).findById("non-existent");
    }

    @Test
    void getAllTransactions_ShouldReturnAllTransactions() {
        List<Transaction> transactions = Arrays.asList(
                transaction,
                new Transaction("test-id-2", "987654321", new BigDecimal("200.00"), "WITHDRAWAL", "Test withdrawal", LocalDateTime.now(), null)
        );
        when(repository.findAll()).thenReturn(transactions);

        List<Transaction> result = service.getAllTransactions();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void deleteTransaction_WhenExisting_ShouldDeleteTransaction() {
        when(repository.existsById("test-id")).thenReturn(true);
        doNothing().when(repository).deleteById("test-id");

        assertDoesNotThrow(() -> service.deleteTransaction("test-id"));
        verify(repository, times(1)).existsById("test-id");
        verify(repository, times(1)).deleteById("test-id");
    }

    @Test
    void deleteTransaction_WhenNotExisting_ShouldThrowException() {
        when(repository.existsById("non-existent")).thenReturn(false);

        assertThrows(TransactionNotFoundException.class, () -> {
            service.deleteTransaction("non-existent");
        });
        verify(repository, times(1)).existsById("non-existent");
        verify(repository, never()).deleteById(anyString());
    }

    @Test
    void updateTransaction_WhenExisting_ShouldUpdateTransaction() {
        when(repository.findById("test-id")).thenReturn(Optional.of(transaction));
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        request.setAmount(new BigDecimal("150.00"));
        request.setDescription("Updated description");

        Transaction result = service.updateTransaction("test-id", request);

        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals("Updated description", result.getDescription());
        verify(repository, times(1)).findById("test-id");
        verify(repository, times(1)).save(any(Transaction.class));
    }
} 
