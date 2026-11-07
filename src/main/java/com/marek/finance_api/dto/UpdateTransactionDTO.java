package com.marek.finance_api.dto;

import com.marek.finance_api.model.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateTransactionDTO(
    @NotNull(message = "Transaction amount is required.")
    @Positive(message = "Transaction amount must be greater than zero.")
    @Digits(integer = 12, fraction = 2,
        message = "Transaction amount cannot exceed 12 integer digits and 2 decimal places.")
    BigDecimal amount,

    @NotNull(message = "Transaction type is required.")
    TransactionType type,

    @NotBlank(message = "Transaction description is required.")
    @Size(max = 255, message = "Transaction description cannot exceed 255 characters.")
    String description,

    @NotNull(message = "Category ID is required.")
    @Positive(message = "Category ID must be greater than zero.")
    Long categoryId
) {}