
package com.marek.finance_api.service;

import com.marek.finance_api.model.Transaction;
import com.marek.finance_api.repository.AccountRepository;
import com.marek.finance_api.repository.CategoryRepository;
import com.marek.finance_api.repository.TransactionRepository;
import com.marek.finance_api.model.Account;
import com.marek.finance_api.model.TransactionType;
import com.marek.finance_api.exception.ResourceNotFoundException;
import com.marek.finance_api.dto.CreateTransactionDTO;
import com.marek.finance_api.model.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Optional;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TransactionServiceTest
{
  @Mock
  private AccountRepository accountRepository;

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private TransactionService transactionService;

  @Test
  void getTransactionsByAccountId_shouldReturnAllTransactions()
  {
    // Arrange
    Long accountId = 4L;

    Account account = mock(Account.class);

    when(account.getId()).thenReturn(accountId);

    Transaction income = new Transaction();
    income.setAccount(account);

    Transaction expense = new Transaction();
    expense.setAccount(account);

    when(accountRepository.existsById(accountId)).thenReturn(true);

    when(transactionRepository.findByAccountId(accountId))
        .thenReturn(List.of(income, expense));

    // Act
    var result = transactionService.getTransactionsByAccountId(
        accountId, null, null, null);

    // Assert
    assertEquals(2, result.size());

    verify(accountRepository).existsById(accountId);
    verify(transactionRepository).findByAccountId(accountId);
  }

  @Test
  void getTransactionsByAccountId_shouldFilterFromDate()
  {
    // Arrange
    Long accountId = 4L;
    LocalDate from = LocalDate.of(2026, 11, 1);
    LocalDateTime startDate = from.atStartOfDay();

    Account account = mock(Account.class);
    when(account.getId()).thenReturn(accountId);

    Transaction transaction = new Transaction();
    transaction.setAccount(account);

    when(accountRepository.existsById(accountId)).thenReturn(true);

    when(transactionRepository.findByAccountIdAndDateGreaterThanEqual(
        accountId, startDate))
        .thenReturn(List.of(transaction));

    // Act
    var result = transactionService.getTransactionsByAccountId(
        accountId, null, from, null);

    // Assert
    assertEquals(1, result.size());

    verify(transactionRepository)
        .findByAccountIdAndDateGreaterThanEqual(accountId, startDate);
  }

  @Test
  void getTransactionsByAccountId_shouldFilterToDate()
  {
    // Arrange
    Long accountId = 4L;
    LocalDate to = LocalDate.of(2026, 11, 6);
    LocalDateTime endDate = to.plusDays(1).atStartOfDay();

    Account account = mock(Account.class);
    when(account.getId()).thenReturn(accountId);

    Transaction transaction = new Transaction();
    transaction.setAccount(account);

    when(accountRepository.existsById(accountId)).thenReturn(true);

    when(transactionRepository.findByAccountIdAndDateLessThan(
        accountId, endDate))
        .thenReturn(List.of(transaction));

    // Act
    var result = transactionService.getTransactionsByAccountId(
        accountId, null, null, to);

    // Assert
    assertEquals(1, result.size());

    verify(transactionRepository)
        .findByAccountIdAndDateLessThan(accountId, endDate);
  }

  @Test
  void getTransactionsByAccountId_shouldFilterDateRange()
  {
    // Arrange
    Long accountId = 4L;
    LocalDate from = LocalDate.of(2026, 11, 1);
    LocalDate to = LocalDate.of(2026, 11, 6);

    LocalDateTime startDate = from.atStartOfDay();
    LocalDateTime endDate = to.plusDays(1).atStartOfDay();

    Account account = mock(Account.class);
    when(account.getId()).thenReturn(accountId);

    Transaction transaction = new Transaction();
    transaction.setAccount(account);

    when(accountRepository.existsById(accountId)).thenReturn(true);

    when(transactionRepository
        .findByAccountIdAndDateGreaterThanEqualAndDateLessThan(
            accountId, startDate, endDate))
        .thenReturn(List.of(transaction));

    // Act
    var result = transactionService.getTransactionsByAccountId(
        accountId, null, from, to);

    // Assert
    assertEquals(1, result.size());

    verify(transactionRepository)
        .findByAccountIdAndDateGreaterThanEqualAndDateLessThan(
            accountId, startDate, endDate);
  }

  @Test
  void getTransactionsByAccountId_shouldFilterByType()
  {
    // Arrange
    Long accountId = 4L;

    Account account = mock(Account.class);
    when(account.getId()).thenReturn(accountId);

    Transaction income = new Transaction();
    income.setAccount(account);
    income.setType(TransactionType.INCOME);

    Transaction expense = new Transaction();
    expense.setAccount(account);
    expense.setType(TransactionType.EXPENSE);

    when(accountRepository.existsById(accountId)).thenReturn(true);

    when(transactionRepository.findByAccountId(accountId))
        .thenReturn(List.of(income, expense));

    // Act
    var result = transactionService.getTransactionsByAccountId(
        accountId, TransactionType.INCOME, null, null);

    // Assert
    assertEquals(1, result.size());
    assertEquals(TransactionType.INCOME, result.get(0).type());

    verify(transactionRepository).findByAccountId(accountId);
  }

  @Test
  void getTransactionsByAccountId_shouldFilterByDateAndType()
  {
    // Arrange
    Long accountId = 4L;
    LocalDate from = LocalDate.of(2026, 11, 1);
    LocalDateTime startDate = from.atStartOfDay();

    Account account = mock(Account.class);
    when(account.getId()).thenReturn(accountId);

    Transaction income = new Transaction();
    income.setAccount(account);
    income.setType(TransactionType.INCOME);

    Transaction expense = new Transaction();
    expense.setAccount(account);
    expense.setType(TransactionType.EXPENSE);

    when(accountRepository.existsById(accountId)).thenReturn(true);

    when(transactionRepository
        .findByAccountIdAndDateGreaterThanEqual(accountId, startDate))
        .thenReturn(List.of(income, expense));

    // Act
    var result = transactionService.getTransactionsByAccountId(
        accountId, TransactionType.INCOME, from, null);

    // Assert
    assertEquals(1, result.size());
    assertEquals(TransactionType.INCOME, result.get(0).type());
  }

  @Test
  void getTransactionsByAccountId_shouldThrowExceptionWhenFromIsAfterTo()
  {
    // Arrange
    Long accountId = 4L;

    LocalDate from = LocalDate.of(2026, 11, 10);
    LocalDate to = LocalDate.of(2026, 11, 5);

    when(accountRepository.existsById(accountId)).thenReturn(true);

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
        transactionService.getTransactionsByAccountId(
            accountId, null, from, to)
    );

    verifyNoInteractions(transactionRepository);
  }

  @Test
  void getTransactionsByAccountId_shouldThrowExceptionWhenAccountDoesNotExist()
  {
    // Arrange
    Long accountId = 999L;

    when(accountRepository.existsById(accountId)).thenReturn(false);

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () ->
        transactionService.getTransactionsByAccountId(
            accountId, null, null, null)
    );

    verify(accountRepository).existsById(accountId);
    verifyNoInteractions(transactionRepository);
  }

  @Test
  void createTransaction_shouldCreateIncomeAndUpdateBalance()
  {
    // Arrange
    Long accountId = 4L;

    Account account = mock(Account.class);

    when(account.getId()).thenReturn(accountId);
    when(account.getBalance()).thenReturn(new BigDecimal("500.00"));

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    mockCategory(1L, "Salary", TransactionType.INCOME);
    CreateTransactionDTO dto = new CreateTransactionDTO(
        new BigDecimal("100.00"),
        TransactionType.INCOME,
        "Salary",
        1L
    );

    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = transactionService.createTransaction(accountId, dto);

    // Assert
    verify(account).setBalance(argThat(balance ->
        balance.compareTo(new BigDecimal("600.00")) == 0
    ));

    verify(transactionRepository).save(argThat(transaction ->
        transaction.getAccount() == account &&
            transaction.getDate() != null
    ));

    assertEquals(accountId, result.accountId());
    assertEquals(new BigDecimal("100.00"), result.amount());
    assertEquals(TransactionType.INCOME, result.type());
    assertEquals(1L, result.categoryId());
    assertEquals("Salary", result.categoryName());
  }

  @Test
  void createTransaction_shouldCreateExpenseAndUpdateBalance() {
    // Arrange
    Long accountId = 4L;

    Account account = mock(Account.class);

    when(account.getId()).thenReturn(accountId);
    when(account.getBalance()).thenReturn(new BigDecimal("500.00"));
    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    mockCategory(1L, "Groceries", TransactionType.EXPENSE);
    CreateTransactionDTO dto = new CreateTransactionDTO(
        new BigDecimal("100.00"),
        TransactionType.EXPENSE,
        "Groceries",
        1L
    );

    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = transactionService.createTransaction(accountId, dto);

    // Assert
    verify(account).setBalance(argThat(balance ->
        balance.compareTo(new BigDecimal("400.00")) == 0
    ));

    verify(transactionRepository).save(any(Transaction.class));

    assertEquals(accountId, result.accountId());
    assertEquals(new BigDecimal("100.00"), result.amount());
    assertEquals(TransactionType.EXPENSE, result.type());
    assertEquals(1L, result.categoryId());
    assertEquals("Groceries", result.categoryName());
  }

  @Test
  void createTransaction_shouldThrowExceptionWhenInsufficientFunds() {
    // Arrange
    Long accountId = 4L;

    Account account = mock(Account.class);

    Category category = mock(Category.class);
    when(category.getType()).thenReturn(TransactionType.EXPENSE);

    when(categoryRepository.findById(1L))
        .thenReturn(Optional.of(category));

    when(account.getBalance()).thenReturn(new BigDecimal("500.00"));
    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    CreateTransactionDTO dto = new CreateTransactionDTO(
        new BigDecimal("600.00"),
        TransactionType.EXPENSE,
        "Rent",
        1L
    );

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () ->
        transactionService.createTransaction(accountId, dto)
    );

    verify(account, never()).setBalance(any(BigDecimal.class));
    verifyNoInteractions(transactionRepository);
  }

  @Test
  void createTransaction_shouldThrowExceptionWhenAccountDoesNotExist() {
    // Arrange
    Long accountId = 999L;

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.empty());

    CreateTransactionDTO dto = new CreateTransactionDTO(
        new BigDecimal("100.00"),
        TransactionType.INCOME,
        "Salary",
        1L
    );

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () ->
        transactionService.createTransaction(accountId, dto)
    );

    verifyNoInteractions(transactionRepository);
  }


  @Test
  void createTransaction_shouldThrowExceptionWhenAmountIsZero() {
    CreateTransactionDTO dto = new CreateTransactionDTO(
        BigDecimal.ZERO,
        TransactionType.INCOME,
        "Invalid transaction",
        1L
    );

    assertThrows(IllegalArgumentException.class, () ->
        transactionService.createTransaction(4L, dto)
    );

    verifyNoInteractions(
        accountRepository,
        transactionRepository,
        categoryRepository
    );
  }

  @Test
  void createTransaction_shouldThrowExceptionWhenAmountIsNegative() {
    CreateTransactionDTO dto = new CreateTransactionDTO(
        new BigDecimal("-100.00"),
        TransactionType.INCOME,
        "Invalid transaction",
        1L
    );

    assertThrows(IllegalArgumentException.class, () ->
        transactionService.createTransaction(4L, dto)
    );

    verifyNoInteractions(
        accountRepository,
        transactionRepository,
        categoryRepository
    );
  }


  @Test
  void createTransaction_shouldThrowExceptionWhenAmountIsNull() {
    CreateTransactionDTO dto = new CreateTransactionDTO(
        null,
        TransactionType.INCOME,
        "Invalid transaction",
        1L
    );

    assertThrows(IllegalArgumentException.class, () ->
        transactionService.createTransaction(4L, dto)
    );

    verifyNoInteractions(
        accountRepository,
        transactionRepository,
        categoryRepository
    );
  }

  @Test
  void createTransaction_shouldThrowExceptionWhenTypeIsNull() {
    CreateTransactionDTO dto = new CreateTransactionDTO(
        new BigDecimal("100.00"),
        null,
        "Invalid transaction",
        1L
    );

    assertThrows(IllegalArgumentException.class, () ->
        transactionService.createTransaction(4L, dto)
    );

    verifyNoInteractions(
        accountRepository,
        transactionRepository,
        categoryRepository
    );
  }

  private void mockCategory(Long id, String name, TransactionType type) {
    Category category = mock(Category.class);

    when(category.getId()).thenReturn(id);
    when(category.getName()).thenReturn(name);
    when(category.getType()).thenReturn(type);

    when(categoryRepository.findById(id))
        .thenReturn(Optional.of(category));
  }


  @Test
  void createTransaction_shouldRejectCategoryWithDifferentType() {
    Long accountId = 4L;

    Account account = mock(Account.class);

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    Category category = mock(Category.class);
    when(category.getType()).thenReturn(TransactionType.EXPENSE);

    when(categoryRepository.findById(1L))
        .thenReturn(Optional.of(category));

    CreateTransactionDTO dto = new CreateTransactionDTO(
        new BigDecimal("100.00"),
        TransactionType.INCOME,
        "Salary",
        1L
    );

    assertThrows(IllegalArgumentException.class, () ->
        transactionService.createTransaction(accountId, dto)
    );

    verifyNoInteractions(transactionRepository);
  }

  @Test
  void createTransaction_shouldRejectNonExistingCategory() {
    Long accountId = 4L;

    Account account = mock(Account.class);

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    when(categoryRepository.findById(999L))
        .thenReturn(Optional.empty());

    CreateTransactionDTO dto = new CreateTransactionDTO(
        new BigDecimal("100.00"),
        TransactionType.INCOME,
        "Salary",
        999L
    );

    assertThrows(ResourceNotFoundException.class, () ->
        transactionService.createTransaction(accountId, dto)
    );

    verifyNoInteractions(transactionRepository);
  }
}
