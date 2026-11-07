package com.marek.finance_api.mapper;

import com.marek.finance_api.dto.CreateTransactionDTO;
import com.marek.finance_api.dto.TransactionDTO;
import com.marek.finance_api.model.Transaction;

public class TransactionMapper
{
  public static TransactionDTO toDTO(Transaction transaction)
  {
    return new TransactionDTO(
        transaction.getId(),
        transaction.getAccount().getId(),
        transaction.getAmount(),
        transaction.getType(),
        transaction.getDescription(),
        transaction.getDate(),
        transaction.getCategory() != null
            ? transaction.getCategory().getId() : null,
        transaction.getCategory() != null
            ? transaction.getCategory().getName() : null
    );
  }

  public static Transaction toEntity(CreateTransactionDTO dto)
  {
    Transaction transaction = new Transaction();

    transaction.setAmount(dto.amount());
    transaction.setType(dto.type());
    transaction.setDescription(dto.description());

    return transaction;
  }
}