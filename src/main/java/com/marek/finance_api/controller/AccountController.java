package com.marek.finance_api.controller;

import com.marek.finance_api.dto.AccountDTO;
import com.marek.finance_api.dto.CreateAccountDTO;
import com.marek.finance_api.dto.UpdateAccountDTO;
import com.marek.finance_api.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@Tag(
    name = "Accounts",
    description = "Endpoints for managing financial accounts"
)
public class AccountController {
  private final AccountService accountService;

  public AccountController(AccountService accountService)
  {
    this.accountService = accountService;
  }


  @GetMapping("/api/accounts")
  @Operation(
      summary = "Get all accounts",
      description = "Returns a list of all financial accounts."
  )
  public List<AccountDTO> getAllAccounts()
  {
    return accountService.getAllAccounts();
  }

  @GetMapping("/api/accounts/{id}")
  @Operation(
      summary = "Get account by ID",
      description = "Returns a financial account with the specified ID."
  )
  public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id)
  {
    AccountDTO account = accountService.getAccountById(id);

    return ResponseEntity.ok(account);
  }

  @PostMapping("/api/accounts")
  @Operation(
      summary = "Create account",
      description = "Creates a new financial account."
  )
  @ApiResponse(
      responseCode = "201",
      description = "Account successfully created"
  )
  public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody CreateAccountDTO dto)
  {
    AccountDTO createdAccount = accountService.createAccount(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
  }

  @DeleteMapping("/api/accounts/{id}")
  @Operation(
      summary = "Delete account",
      description = "Deletes a financial account with the specified ID."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Account successfully deleted"),
      @ApiResponse(responseCode = "404", description = "Account not found"),
      @ApiResponse(
          responseCode = "409",
          description = "Account cannot be deleted because it contains transactions"
      )
  })
  public ResponseEntity<Void> deleteAccount(@PathVariable Long id)
  {
    accountService.deleteAccount(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/api/accounts/{id}")
  @Operation(
      summary = "Update account",
      description = "Updates the name of an existing financial account."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Account successfully updated"
  )
  @ApiResponse(
      responseCode = "409",
      description = "Account was modified by another request"
  )
  public ResponseEntity<AccountDTO> updateAccount(
      @PathVariable Long id, @Valid @RequestBody UpdateAccountDTO dto)
  {
    AccountDTO updatedAccount = accountService.updateAccount(id, dto);
    return ResponseEntity.ok(updatedAccount);
  }
}