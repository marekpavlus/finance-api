
package com.marek.finance_api.service;

import com.marek.finance_api.dto.CreateAccountDTO;
import com.marek.finance_api.exception.ResourceConflictException;
import com.marek.finance_api.exception.ResourceNotFoundException;
import com.marek.finance_api.model.Account;
import com.marek.finance_api.repository.AccountRepository;
import com.marek.finance_api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest
{
  @Mock
  private AccountRepository accountRepository;

  @Mock
  private TransactionRepository transactionRepository;

  @InjectMocks
  private AccountService accountService;

  private Account account;

  @BeforeEach
  void setUp()
  {
    account = new Account();
    account.setName("Main Account");
    account.setBalance(new BigDecimal("1000.00"));
    account.setCurrency("EUR");
  }

  @Test
  void shouldDeleteAccountWithoutTransactions()
  {
    when(accountRepository.findById(1L))
        .thenReturn(Optional.of(account));
    when(transactionRepository.existsByAccountId(1L))
        .thenReturn(false);

    assertDoesNotThrow(() -> accountService.deleteAccount(1L));

    verify(accountRepository).delete(account);
  }

  @Test
  void shouldThrowExceptionWhenDeletingAccountWithTransactions()
  {
    when(accountRepository.findById(1L))
        .thenReturn(Optional.of(account));
    when(transactionRepository.existsByAccountId(1L))
        .thenReturn(true);

    assertThrows(
        ResourceConflictException.class,
        () -> accountService.deleteAccount(1L)
    );

    verify(accountRepository, never()).delete(any(Account.class));
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistingAccount()
  {
    when(accountRepository.findById(1L))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> accountService.deleteAccount(1L)
    );

    verify(transactionRepository, never()).existsByAccountId(anyLong());
    verify(accountRepository, never()).delete(any(Account.class));
  }
}
