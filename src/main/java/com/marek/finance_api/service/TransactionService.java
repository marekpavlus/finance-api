package com.marek.finance_api.service;

import com.marek.finance_api.dto.CreateTransactionDTO;
import com.marek.finance_api.dto.TransactionDTO;
import com.marek.finance_api.mapper.TransactionMapper;
import com.marek.finance_api.model.Account;
import com.marek.finance_api.model.Transaction;
import com.marek.finance_api.model.TransactionType;
import com.marek.finance_api.repository.AccountRepository;
import com.marek.finance_api.repository.TransactionRepository;
import com.marek.finance_api.dto.UpdateTransactionDTO;
import com.marek.finance_api.model.Category;
import com.marek.finance_api.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.marek.finance_api.exception.ResourceNotFoundException;

@Service
public class TransactionService
{

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final CategoryRepository categoryRepository;

  public TransactionService(
      AccountRepository accountRepository,
      TransactionRepository transactionRepository,
      CategoryRepository categoryRepository)
  {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.categoryRepository = categoryRepository;
  }

  @Transactional public TransactionDTO createTransaction(Long accountId,
      CreateTransactionDTO dto)
  {
    Transaction transaction = TransactionMapper.toEntity(dto);

    if (transaction.getAmount() == null
        || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0)
    {
      throw new IllegalArgumentException(
          "Transaction amount must be greater than zero.");
    }

    if (transaction.getType() == null)
    {
      throw new IllegalArgumentException("Transaction type is required.");
    }

    Account account = accountRepository.findById(accountId).orElseThrow(
        () -> new ResourceNotFoundException(
            "Account with id " + accountId + " not found."));
    TransactionType type = transaction.getType();

    Category category = findAndValidateCategory(
        dto.categoryId(), type
    );
    transaction.setCategory(category);

    if (type == TransactionType.INCOME)
    {
      account.setBalance(account.getBalance().add(transaction.getAmount()));
    }
    else if (type == TransactionType.EXPENSE)
    {
      if (account.getBalance().compareTo(transaction.getAmount()) < 0)
      {
        throw new IllegalArgumentException("Insufficient funds.");
      }

      account.setBalance(account.getBalance().subtract(transaction.getAmount()));

    }

    transaction.setAccount(account);
    transaction.setDate(LocalDateTime.now());

    Transaction savedTransaction = transactionRepository.save(transaction);

    return TransactionMapper.toDTO(savedTransaction);
  }

  public List<TransactionDTO> getTransactionsByAccountId(
      Long accountId,
      TransactionType type,
      LocalDate from,
      LocalDate to)
  {
    if (!accountRepository.existsById(accountId))
    {
      throw new ResourceNotFoundException(
          "Account with id " + accountId + " not found.");
    }

    if (from != null && to != null && from.isAfter(to))
    {
      throw new IllegalArgumentException(
          "Start date cannot be after end date.");
    }

    LocalDateTime startDate = from != null ? from.atStartOfDay() : null;
    LocalDateTime endDate = to != null ? to.plusDays(1).atStartOfDay() : null;

    List<Transaction> transactions;

    if (from != null && to != null)
    {
      transactions = transactionRepository
          .findByAccountIdAndDateGreaterThanEqualAndDateLessThan(
              accountId, startDate, endDate);
    }
    else if (from != null)
    {
      transactions = transactionRepository
          .findByAccountIdAndDateGreaterThanEqual(accountId, startDate);
    }
    else if (to != null)
    {
      transactions = transactionRepository
          .findByAccountIdAndDateLessThan(accountId, endDate);
    }
    else
    {
      transactions = transactionRepository.findByAccountId(accountId);
    }

    return transactions.stream()
        .filter(transaction -> type == null || transaction.getType() == type)
        .map(TransactionMapper::toDTO)
        .toList();
  }

  @Transactional
  public TransactionDTO updateTransaction(Long id, UpdateTransactionDTO dto)
  {
    Transaction transaction = transactionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Transaction with id " + id + " not found."));

    Category category = findAndValidateCategory(dto.categoryId(), dto.type());

    Account account = transaction.getAccount();

    // Reverse the original transaction.
    reverseBalanceEffect(account, transaction);

    // Apply the updated transaction.
    applyBalanceEffect(account, dto.type(), dto.amount());

    transaction.setAmount(dto.amount());
    transaction.setType(dto.type());
    transaction.setDescription(dto.description());
    transaction.setCategory(category);

    return TransactionMapper.toDTO(transaction);
  }

  @Transactional
  public void deleteTransaction(Long id)
  {
    Transaction transaction = transactionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Transaction with id " + id + " not found."));

    reverseBalanceEffect(transaction.getAccount(), transaction);

    transactionRepository.delete(transaction);
  }


  private void reverseBalanceEffect(Account account, Transaction transaction)
  {
    if (transaction.getType() == TransactionType.INCOME)
    {
      if (account.getBalance().compareTo(transaction.getAmount()) < 0)
      {
        throw new IllegalArgumentException(
            "Transaction cannot be reversed because the income has already been spent."
        );
      }

      account.setBalance(
          account.getBalance().subtract(transaction.getAmount())
      );
    }
    else
    {
      account.setBalance(
          account.getBalance().add(transaction.getAmount())
      );
    }
  }


  private void applyBalanceEffect(
      Account account,
      TransactionType type,
      BigDecimal amount)
  {
    if (type == TransactionType.INCOME)
    {
      account.setBalance(account.getBalance().add(amount));
    }
    else
    {
      if (account.getBalance().compareTo(amount) < 0)
      {
        throw new IllegalArgumentException("Insufficient funds.");
      }

      account.setBalance(account.getBalance().subtract(amount));
    }
  }

  private Category findAndValidateCategory(Long categoryId, TransactionType type)
  {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Category with id " + categoryId + " not found."));

    if (category.getType() != type)
    {
      throw new IllegalArgumentException(
          "Category type must match transaction type.");
    }

    return category;
  }
}

