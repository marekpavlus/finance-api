
package com.marek.finance_api.repository;

import com.marek.finance_api.model.Account;
import com.marek.finance_api.model.Transaction;
import com.marek.finance_api.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class TransactionRepositoryIntegrationTest
{
  @Container
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18-alpine");

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry)
  {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Test
  void shouldSaveAndFindTransactionWithAccount()
  {
    // Arrange: create account
    Account account = new Account();
    account.setName("Test Account");
    account.setBalance(new BigDecimal("1000.00"));
    account.setCurrency("EUR");

    Account savedAccount = accountRepository.saveAndFlush(account);

    // Arrange: create transaction
    Transaction transaction = new Transaction();
    transaction.setAmount(new BigDecimal("150.00"));
    transaction.setType(TransactionType.EXPENSE);
    transaction.setDescription("Groceries");
    transaction.setDate(LocalDateTime.of(2026, 11, 6, 12, 0));
    transaction.setAccount(savedAccount);

    // Act: save and retrieve transaction
    Transaction saved = transactionRepository.saveAndFlush(transaction);

    Transaction found = transactionRepository.findById(saved.getId())
        .orElseThrow();

    // Assert
    assertNotNull(found.getId());
    assertEquals(new BigDecimal("150.00"), found.getAmount());
    assertEquals(TransactionType.EXPENSE, found.getType());
    assertEquals("Groceries", found.getDescription());
    assertEquals(savedAccount.getId(), found.getAccount().getId());

    // Verify repository query
    assertEquals(
        1,
        transactionRepository.findByAccountId(savedAccount.getId()).size()
    );
  }


  @Test
  void shouldFilterTransactionsByTypeAndDate()
  {
    Account account = new Account();
    account.setName("Filter Test Account");
    account.setBalance(new BigDecimal("2000.00"));
    account.setCurrency("EUR");

    Account savedAccount = accountRepository.saveAndFlush(account);

    Transaction income = new Transaction();
    income.setAmount(new BigDecimal("2000.00"));
    income.setType(TransactionType.INCOME);
    income.setDescription("Salary");
    income.setDate(LocalDateTime.of(2026, 11, 1, 10, 0));
    income.setAccount(savedAccount);

    Transaction expense = new Transaction();
    expense.setAmount(new BigDecimal("150.00"));
    expense.setType(TransactionType.EXPENSE);
    expense.setDescription("Groceries");
    expense.setDate(LocalDateTime.of(2026, 11, 6, 12, 0));
    expense.setAccount(savedAccount);

    Transaction oldExpense = new Transaction();
    oldExpense.setAmount(new BigDecimal("50.00"));
    oldExpense.setType(TransactionType.EXPENSE);
    oldExpense.setDescription("Old purchase");
    oldExpense.setDate(LocalDateTime.of(2026, 10, 25, 12, 0));
    oldExpense.setAccount(savedAccount);

    transactionRepository.saveAllAndFlush(
        java.util.List.of(income, expense, oldExpense)
    );

    Long accountId = savedAccount.getId();
    LocalDateTime from = LocalDateTime.of(2026, 11, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2026, 11, 7, 0, 0);

    assertEquals(3,
        transactionRepository.findByAccountId(accountId).size());

    assertEquals(1,
        transactionRepository.findByAccountIdAndType(
            accountId, TransactionType.INCOME
        ).size());

    assertEquals(2,
        transactionRepository.findByAccountIdAndDateGreaterThanEqual(
            accountId, from
        ).size());

    assertEquals(3,
        transactionRepository.findByAccountIdAndDateLessThan(
            accountId, to
        ).size());

    assertEquals(2,
        transactionRepository.findByAccountIdAndDateGreaterThanEqualAndDateLessThan(
            accountId, from, to
        ).size());
  }

}
