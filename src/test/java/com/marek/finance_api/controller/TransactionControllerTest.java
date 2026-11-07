
package com.marek.finance_api.controller;

import com.marek.finance_api.dto.CreateTransactionDTO;
import com.marek.finance_api.dto.TransactionDTO;
import com.marek.finance_api.dto.UpdateTransactionDTO;
import com.marek.finance_api.exception.ResourceNotFoundException;
import com.marek.finance_api.model.TransactionType;
import com.marek.finance_api.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TransactionService transactionService;

  private TransactionDTO transactionDTO() {
    return new TransactionDTO(
        1L,
        1L,
        new BigDecimal("100.00"),
        TransactionType.EXPENSE,
        "Groceries",
        LocalDateTime.of(2026, 11, 6, 12, 0),
        1L,
        "Groceries"
    );
  }

  @Test
  void shouldCreateTransaction() throws Exception {
    when(transactionService.createTransaction(
        eq(1L), any(CreateTransactionDTO.class)))
        .thenReturn(transactionDTO());

    mockMvc.perform(post("/api/accounts/1/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                         "amount": 100.00,
                         "type": "EXPENSE",
                         "description": "Groceries",
                         "categoryId": 1
                     }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.accountId").value(1))
        .andExpect(jsonPath("$.amount").value(100.00))
        .andExpect(jsonPath("$.type").value("EXPENSE"))
        .andExpect(jsonPath("$.description").value("Groceries"))
        .andExpect(jsonPath("$.categoryId").value(1))
        .andExpect(jsonPath("$.categoryName").value("Groceries"));
  }

  @Test
  void shouldReturn400WhenCreatingInvalidTransaction() throws Exception {
    mockMvc.perform(post("/api/accounts/1/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "amount": -100.00,
                        "type": "EXPENSE",
                        "description": "Groceries",
                        "categoryId": 1
                    }
                    """))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
  }

  @Test
  void shouldReturnTransactionsByAccountId() throws Exception {
    when(transactionService.getTransactionsByAccountId(
        eq(1L), isNull(), isNull(), isNull()))
        .thenReturn(List.of(transactionDTO()));

    mockMvc.perform(get("/api/accounts/1/transactions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].description").value("Groceries"));
  }

  @Test
  void shouldFilterTransactionsByTypeAndDate() throws Exception {
    LocalDate from = LocalDate.of(2026, 11, 1);
    LocalDate to = LocalDate.of(2026, 11, 6);

    when(transactionService.getTransactionsByAccountId(
        eq(1L), eq(TransactionType.EXPENSE), eq(from), eq(to)))
        .thenReturn(List.of(transactionDTO()));

    mockMvc.perform(get("/api/accounts/1/transactions")
            .param("type", "EXPENSE")
            .param("from", "2026-11-01")
            .param("to", "2026-11-06"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    verify(transactionService).getTransactionsByAccountId(
        1L, TransactionType.EXPENSE, from, to);
  }

  @Test
  void shouldReturn400WhenDateRangeIsInvalid() throws Exception {
    when(transactionService.getTransactionsByAccountId(
        eq(1L), isNull(),
        eq(LocalDate.of(2026, 11, 7)),
        eq(LocalDate.of(2026, 11, 1))))
        .thenThrow(new IllegalArgumentException(
            "Start date cannot be after end date."));

    mockMvc.perform(get("/api/accounts/1/transactions")
            .param("from", "2026-11-07")
            .param("to", "2026-11-01"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldUpdateTransaction() throws Exception {
    when(transactionService.updateTransaction(
        eq(1L), any(UpdateTransactionDTO.class)))
        .thenReturn(transactionDTO());

    mockMvc.perform(put("/api/transactions/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "amount": 100.00,
                        "type": "EXPENSE",
                        "description": "Groceries",
                        "categoryId": 1
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.description").value("Groceries"));
  }

  @Test
  void shouldReturn404WhenUpdatingNonExistingTransaction() throws Exception {
    when(transactionService.updateTransaction(
        eq(1L), any(UpdateTransactionDTO.class)))
        .thenThrow(new ResourceNotFoundException(
            "Transaction not found."));

    mockMvc.perform(put("/api/transactions/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "amount": 100.00,
                        "type": "EXPENSE",
                        "description": "Groceries",
                        "categoryId": 1
                    }
                    """))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenTransactionHasInsufficientFunds() throws Exception {
    when(transactionService.updateTransaction(
        eq(1L), any(UpdateTransactionDTO.class)))
        .thenThrow(new IllegalArgumentException(
            "Insufficient account balance."));

    mockMvc.perform(put("/api/transactions/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "amount": 5000.00,
                        "type": "EXPENSE",
                        "description": "Expensive purchase",
                        "categoryId": 1
                    }
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldDeleteTransaction() throws Exception {
    doNothing().when(transactionService).deleteTransaction(1L);

    mockMvc.perform(delete("/api/transactions/1"))
        .andExpect(status().isNoContent());

    verify(transactionService).deleteTransaction(1L);
  }

  @Test
  void shouldReturn404WhenDeletingNonExistingTransaction() throws Exception {
    doThrow(new ResourceNotFoundException("Transaction not found."))
        .when(transactionService).deleteTransaction(1L);

    mockMvc.perform(delete("/api/transactions/1"))
        .andExpect(status().isNotFound());
  }
}
