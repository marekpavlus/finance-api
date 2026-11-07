
package com.marek.finance_api.service;

import com.marek.finance_api.exception.ResourceConflictException;
import com.marek.finance_api.exception.ResourceNotFoundException;
import com.marek.finance_api.model.Account;
import com.marek.finance_api.model.Transaction;
import com.marek.finance_api.model.TransactionType;
import com.marek.finance_api.repository.AccountRepository;
import com.marek.finance_api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class AccountServiceIntegrationTest
{
  @Container
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18-alpine");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry)
  {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private AccountService accountService;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  private Account account;

  @BeforeEach
  void setUp()
  {
    transactionRepository.deleteAll();
    accountRepository.deleteAll();

    account = new Account();
    account.setName("Test Account");
    account.setBalance(new BigDecimal("1000.00"));
    account.setCurrency("EUR");

    account = accountRepository.saveAndFlush(account);
  }

  @Test
  void shouldDeleteAccountWithoutTransactions()
  {
    Long accountId = account.getId();

    assertDoesNotThrow(() -> accountService.deleteAccount(accountId));

    assertFalse(accountRepository.existsById(accountId));
  }

  @Test
  void shouldNotDeleteAccountWithTransactions()
  {
    Transaction transaction = new Transaction();
    transaction.setAmount(new BigDecimal("100.00"));
    transaction.setType(TransactionType.EXPENSE);
    transaction.setDescription("Groceries");
    transaction.setDate(LocalDateTime.now());
    transaction.setAccount(account);

    transactionRepository.saveAndFlush(transaction);

    assertThrows(
        ResourceConflictException.class,
        () -> accountService.deleteAccount(account.getId())
    );

    assertTrue(accountRepository.existsById(account.getId()));
    assertEquals(1, transactionRepository.findByAccountId(account.getId()).size());
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistingAccount()
  {
    assertThrows(
        ResourceNotFoundException.class,
        () -> accountService.deleteAccount(-1L)
    );
  }
}