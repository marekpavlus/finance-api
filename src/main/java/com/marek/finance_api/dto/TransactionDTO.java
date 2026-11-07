package com.marek.finance_api.dto;

import com.marek.finance_api.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
    Long id,
    Long accountId,
    BigDecimal amount,
    TransactionType type,
    String description,
    LocalDateTime date,
    Long categoryId,
    String categoryName
) {}