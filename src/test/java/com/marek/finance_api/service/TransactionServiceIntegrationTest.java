
package com.marek.finance_api.service;

import com.marek.finance_api.dto.UpdateTransactionDTO;
import org.testcontainers.postgresql.PostgreSQLContainer;
import com.marek.finance_api.dto.CreateTransactionDTO;
import com.marek.finance_api.exception.ResourceNotFoundException;
import com.marek.finance_api.model.Account;
import com.marek.finance_api.model.Transaction;
import com.marek.finance_api.model.TransactionType;
import com.marek.finance_api.repository.AccountRepository;
import com.marek.finance_api.repository.TransactionRepository;
import com.marek.finance_api.model.Category;
import com.marek.finance_api.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class TransactionServiceIntegrationTest {

  @BeforeEach
  void cleanDatabase() {
    transactionRepository.deleteAll();
    categoryRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Container
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18-alpine");

  @Autowired
  private TransactionService transactionService;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  private Account createAccount(String balance) {
    Account account = new Account();
    account.setName("Integration Test Account");
    account.setBalance(new BigDecimal(balance));
    account.setCurrency("EUR");
    return accountRepository.saveAndFlush(account);
  }

  private Transaction createTransaction(
      Account account, String amount, TransactionType type
  ) {
    Category category = createCategory(type, "Test " + type);

    Transaction transaction = new Transaction();
    transaction.setAccount(account);
    transaction.setCategory(category);
    transaction.setAmount(new BigDecimal(amount));
    transaction.setType(type);
    transaction.setDescription("Test transaction");
    transaction.setDate(LocalDateTime.now());

    return transactionRepository.saveAndFlush(transaction);
  }

  @Test
  void shouldUpdateExpenseAndAdjustBalance() {
    Account account = createAccount("900.00");
    Transaction transaction =
        createTransaction(account, "100.00", TransactionType.EXPENSE);

    UpdateTransactionDTO dto = new UpdateTransactionDTO(
        new BigDecimal("200.00"),
        TransactionType.EXPENSE,
        "Updated expense",
        transaction.getCategory().getId()
    );

    transactionService.updateTransaction(transaction.getId(), dto);

    assertEquals(
        0,
        new BigDecimal("800.00").compareTo(
            accountRepository.findById(account.getId())
                .orElseThrow().getBalance()
        )
    );

    Transaction updated = transactionRepository
        .findById(transaction.getId()).orElseThrow();

    assertEquals(new BigDecimal("200.00"), updated.getAmount());
    assertEquals("Updated expense", updated.getDescription());
  }

  @Test
  void shouldUpdateExpenseToIncome() {
    Account account = createAccount("900.00");
    Transaction transaction =
        createTransaction(account, "100.00", TransactionType.EXPENSE);

    Category incomeCategory = createCategory(
        TransactionType.INCOME, "Salary"
    );

    UpdateTransactionDTO dto = new UpdateTransactionDTO(
        new BigDecimal("200.00"),
        TransactionType.INCOME,
        "Salary",
        incomeCategory.getId()
    );

    transactionService.updateTransaction(transaction.getId(), dto);

    assertEquals(
        0,
        new BigDecimal("1200.00").compareTo(
            accountRepository.findById(account.getId())
                .orElseThrow().getBalance()
        )
    );
  }

  @Test
  void shouldUpdateIncomeToExpense() {
    Account account = createAccount("1200.00");
    Transaction transaction =
        createTransaction(account, "200.00", TransactionType.INCOME);

    Category expenseCategory = createCategory(
        TransactionType.EXPENSE, "Rent"
    );

    UpdateTransactionDTO dto = new UpdateTransactionDTO(
        new BigDecimal("300.00"),
        TransactionType.EXPENSE,
        "Rent",
        expenseCategory.getId()
    );

    transactionService.updateTransaction(transaction.getId(), dto);

    assertEquals(
        0,
        new BigDecimal("700.00").compareTo(
            accountRepository.findById(account.getId())
                .orElseThrow().getBalance()
        )
    );
  }

  @Test
  void shouldDeleteExpenseAndRestoreBalance() {
    Account account = createAccount("900.00");
    Transaction transaction =
        createTransaction(account, "100.00", TransactionType.EXPENSE);

    transactionService.deleteTransaction(transaction.getId());

    assertEquals(
        0,
        new BigDecimal("1000.00").compareTo(
            accountRepository.findById(account.getId())
                .orElseThrow().getBalance()
        )
    );

    assertFalse(transactionRepository.existsById(transaction.getId()));
  }

  @Test
  void shouldDeleteIncomeAndReverseBalance() {
    Account account = createAccount("1200.00");
    Transaction transaction =
        createTransaction(account, "200.00", TransactionType.INCOME);

    transactionService.deleteTransaction(transaction.getId());

    assertEquals(
        0,
        new BigDecimal("1000.00").compareTo(
            accountRepository.findById(account.getId())
                .orElseThrow().getBalance()
        )
    );

    assertFalse(transactionRepository.existsById(transaction.getId()));
  }


  @Test
  void shouldRejectDeletingIncomeWhenAlreadySpent()
  {
    Account account = createAccount("0.00");
    Transaction income = createTransaction(
        account, "500.00", TransactionType.INCOME
    );

    account.setBalance(new BigDecimal("100.00"));
    accountRepository.saveAndFlush(account);

    assertThrows(IllegalArgumentException.class, () ->
        transactionService.deleteTransaction(income.getId())
    );

    Account unchangedAccount = accountRepository
        .findById(account.getId()).orElseThrow();

    Transaction unchangedTransaction = transactionRepository
        .findById(income.getId()).orElseThrow();

    assertEquals(
        0,
        new BigDecimal("100.00").compareTo(unchangedAccount.getBalance())
    );
    assertEquals(TransactionType.INCOME, unchangedTransaction.getType());
    assertEquals(new BigDecimal("500.00"), unchangedTransaction.getAmount());
  }


  @Test
  void shouldRollbackUpdateWhenInsufficientFunds() {
    Account account = createAccount("200.00");
    Transaction transaction =
        createTransaction(account, "100.00", TransactionType.INCOME);

    Category expenseCategory = createCategory(
        TransactionType.EXPENSE, "Too expensive"
    );

    UpdateTransactionDTO dto = new UpdateTransactionDTO(
        new BigDecimal("500.00"),
        TransactionType.EXPENSE,
        "Too expensive",
        expenseCategory.getId()
    );

    assertThrows(IllegalArgumentException.class, () ->
        transactionService.updateTransaction(transaction.getId(), dto)
    );

    Account unchangedAccount = accountRepository
        .findById(account.getId()).orElseThrow();

    Transaction unchangedTransaction = transactionRepository
        .findById(transaction.getId()).orElseThrow();

    assertEquals(
        0,
        new BigDecimal("200.00").compareTo(unchangedAccount.getBalance())
    );
    assertEquals(TransactionType.INCOME, unchangedTransaction.getType());
    assertEquals(new BigDecimal("100.00"), unchangedTransaction.getAmount());
    assertEquals("Test transaction", unchangedTransaction.getDescription());
  }


  @Test
  void shouldRollbackUpdateWhenIncomeCannotBeReversed()
  {
    Account account = createAccount("0.00");
    Transaction income = createTransaction(
        account, "500.00", TransactionType.INCOME
    );

    account.setBalance(new BigDecimal("100.00"));
    accountRepository.saveAndFlush(account);

    Category expenseCategory = createCategory(
        TransactionType.EXPENSE, "Updated transaction"
    );

    UpdateTransactionDTO dto = new UpdateTransactionDTO(
        new BigDecimal("200.00"),
        TransactionType.EXPENSE,
        "Updated transaction",
        expenseCategory.getId()
    );

    assertThrows(IllegalArgumentException.class, () ->
        transactionService.updateTransaction(income.getId(), dto)
    );

    Account unchangedAccount = accountRepository
        .findById(account.getId()).orElseThrow();

    Transaction unchangedTransaction = transactionRepository
        .findById(income.getId()).orElseThrow();

    assertEquals(
        0,
        new BigDecimal("100.00").compareTo(unchangedAccount.getBalance())
    );
    assertEquals(TransactionType.INCOME, unchangedTransaction.getType());
    assertEquals(new BigDecimal("500.00"), unchangedTransaction.getAmount());
    assertEquals("Test transaction", unchangedTransaction.getDescription());
  }


  @Test
  void shouldThrowExceptionWhenTransactionDoesNotExist() {
    assertThrows(ResourceNotFoundException.class, () ->
        transactionService.deleteTransaction(-1L)
    );
  }


  @Test
  void shouldPreventOverspendingDuringConcurrentExpenses() throws Exception
  {
    Account account = createAccount("100.00");
    Category expenseCategory = createCategory(
        TransactionType.EXPENSE, "Concurrent expense"
    );

    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);

    try
    {
      var expenseTask = (java.util.concurrent.Callable<Boolean>) () -> {
        ready.countDown();
        start.await();

        try {
          transactionService.createTransaction(
              account.getId(),
              new CreateTransactionDTO(
                  new BigDecimal("80.00"),
                  TransactionType.EXPENSE,
                  "Concurrent expense",
                  expenseCategory.getId()
              )
          );
          return true;
        } catch (IllegalArgumentException |
                 OptimisticLockingFailureException exception) {
          return false;
        }
      };

      Future<Boolean> first = executor.submit(expenseTask);
      Future<Boolean> second = executor.submit(expenseTask);

      assertTrue(ready.await(5, TimeUnit.SECONDS));
      start.countDown();

      assertNotEquals(first.get(10, TimeUnit.SECONDS),
          second.get(10, TimeUnit.SECONDS));

      Account updatedAccount = accountRepository
          .findById(account.getId()).orElseThrow();

      assertEquals(
          0,
          new BigDecimal("20.00").compareTo(updatedAccount.getBalance())
      );

      assertEquals(
          1,
          transactionRepository.findByAccountId(account.getId()).size()
      );
    }
    finally
    {
      executor.shutdownNow();
    }
  }

  private Category createCategory(TransactionType type, String name) {
    Category category = new Category();
    category.setName(name);
    category.setType(type);
    return categoryRepository.saveAndFlush(category);
  }
}
