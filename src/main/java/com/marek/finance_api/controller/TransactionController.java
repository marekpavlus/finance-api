package com.marek.finance_api.controller;

import com.marek.finance_api.dto.CreateTransactionDTO;
import com.marek.finance_api.dto.TransactionDTO;
import com.marek.finance_api.model.TransactionType;
import com.marek.finance_api.service.TransactionService;
import com.marek.finance_api.dto.UpdateTransactionDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(
    name = "Transactions",
    description = "Endpoints for managing financial transactions"
)
public class TransactionController {
  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService)
  {
    this.transactionService = transactionService;
  }

  @PostMapping("/api/accounts/{accountId}/transactions")
  @Operation(
      summary = "Create transaction",
      description = "Creates a new income or expense transaction for the specified account."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Transaction successfully created"),
      @ApiResponse(responseCode = "404", description = "Account not found"),
      @ApiResponse(responseCode = "400", description = "Invalid transaction data")
  })
  public ResponseEntity<TransactionDTO> createTransaction(
      @PathVariable Long accountId,
      @Valid @RequestBody CreateTransactionDTO dto)
  {
    TransactionDTO createdTransaction =
        transactionService.createTransaction(accountId, dto);

    return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
  }

  @GetMapping("/api/accounts/{accountId}/transactions")
  @Operation(
      summary = "Get account transactions",
      description = "Returns transactions for a specific account. Results can be filtered by transaction type and date range."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Transactions successfully retrieved"
  )
  public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountId(
      @PathVariable Long accountId,
      @RequestParam(required = false) TransactionType type,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
  {
    List<TransactionDTO> transactions =
        transactionService.getTransactionsByAccountId(
            accountId, type, from, to);

    return ResponseEntity.ok(transactions);
  }

  @PutMapping("/api/transactions/{id}")
  public ResponseEntity<TransactionDTO> updateTransaction(
      @PathVariable Long id,
      @Valid @RequestBody UpdateTransactionDTO dto)
  {
    return ResponseEntity.ok(
        transactionService.updateTransaction(id, dto)
    );
  }

  @DeleteMapping("/api/transactions/{id}")
  public ResponseEntity<Void> deleteTransaction(@PathVariable Long id)
  {
    transactionService.deleteTransaction(id);
    return ResponseEntity.noContent().build();
  }
}