package com.marek.finance_api.repository;

import com.marek.finance_api.model.Transaction;
import com.marek.finance_api.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>
{
  List<Transaction> findByAccountId(Long accountId);

  List<Transaction> findByAccountIdAndType(
      Long accountId, TransactionType type);

  List<Transaction> findByAccountIdAndDateGreaterThanEqual(
      Long accountId, LocalDateTime from);

  List<Transaction> findByAccountIdAndDateLessThan(
      Long accountId, LocalDateTime to);

  List<Transaction> findByAccountIdAndDateGreaterThanEqualAndDateLessThan(
      Long accountId, LocalDateTime from, LocalDateTime to);

  boolean existsByAccountId(Long accountId);
  boolean existsByCategoryId(Long categoryId);
}