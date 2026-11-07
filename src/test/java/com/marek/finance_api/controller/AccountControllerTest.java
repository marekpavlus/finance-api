
package com.marek.finance_api.controller;

import com.marek.finance_api.dto.AccountDTO;
import com.marek.finance_api.dto.CreateAccountDTO;
import com.marek.finance_api.dto.UpdateAccountDTO;
import com.marek.finance_api.model.Account;
import com.marek.finance_api.service.AccountService;
import com.marek.finance_api.exception.ResourceConflictException;
import com.marek.finance_api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AccountService accountService;

  private AccountDTO accountDTO() {
    return new AccountDTO(
        1L, "Main Account", new BigDecimal("1000.00"), "EUR"
    );
  }

  @Test
  void shouldReturnAllAccounts() throws Exception {
    when(accountService.getAllAccounts()).thenReturn(List.of(accountDTO()));

    mockMvc.perform(get("/api/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Main Account"));
  }

  @Test
  void shouldReturnAccountById() throws Exception {
    when(accountService.getAccountById(1L)).thenReturn(accountDTO());

    mockMvc.perform(get("/api/accounts/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Main Account"))
        .andExpect(jsonPath("$.balance").value(1000.00))
        .andExpect(jsonPath("$.currency").value("EUR"));
  }

  @Test
  void shouldReturn404WhenAccountDoesNotExist() throws Exception {
    when(accountService.getAccountById(1L))
        .thenThrow(new ResourceNotFoundException("Account not found."));

    mockMvc.perform(get("/api/accounts/1"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Account not found."));
  }

  @Test
  void shouldCreateAccount() throws Exception {
    when(accountService.createAccount(any(CreateAccountDTO.class)))
        .thenReturn(accountDTO());

    mockMvc.perform(post("/api/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "name": "Main Account",
                        "currency": "EUR"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Main Account"));
  }

  @Test
  void shouldReturn400WhenCreatingAccountWithBlankName() throws Exception {
    mockMvc.perform(post("/api/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "name": "",
                        "currency": "EUR"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name")
            .value("Account name is required."));

    verifyNoInteractions(accountService);
  }

  @Test
  void shouldUpdateAccount() throws Exception {
    when(accountService.updateAccount(eq(1L), any(UpdateAccountDTO.class)))
        .thenReturn(new AccountDTO(
            1L, "Updated account", new BigDecimal("1000.00"), "EUR"
        ));

    mockMvc.perform(put("/api/accounts/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "name": "Updated account"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated account"));
  }

  @Test
  void shouldReturn400WhenUpdatingAccountWithBlankName() throws Exception {
    mockMvc.perform(put("/api/accounts/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "name": " "
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name")
            .value("Account name is required."));

    verifyNoInteractions(accountService);
  }

  @Test
  void shouldReturn409WhenOptimisticLockingFails() throws Exception {
    when(accountService.updateAccount(eq(1L), any(UpdateAccountDTO.class)))
        .thenThrow(new ObjectOptimisticLockingFailureException(Account.class, 1L));

    mockMvc.perform(put("/api/accounts/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "name": "Updated account"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(content().string("Resource was modified by another operation. Please refresh and try again."));
  }

  @Test
  void shouldDeleteAccount() throws Exception {
    doNothing().when(accountService).deleteAccount(1L);

    mockMvc.perform(delete("/api/accounts/1"))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    verify(accountService).deleteAccount(1L);
  }

  @Test
  void shouldReturn409WhenDeletingAccountWithTransactions() throws Exception {
    doThrow(new ResourceConflictException("Account contains transactions."))
        .when(accountService).deleteAccount(1L);

    mockMvc.perform(delete("/api/accounts/1"))
        .andExpect(status().isConflict())
        .andExpect(content().string("Account contains transactions."));
  }

  @Test
  void shouldReturn409WhenDataIntegrityViolationOccurs() throws Exception {
    doThrow(new DataIntegrityViolationException("Database constraint violation"))
        .when(accountService).deleteAccount(1L);

    mockMvc.perform(delete("/api/accounts/1"))
        .andExpect(status().isConflict())
        .andExpect(content().string("Operation conflicts with existing data."));
  }
}
