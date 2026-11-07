package com.marek.finance_api.dto;

import java.math.BigDecimal;

public record AccountDTO(
    Long id,
    String name,
    BigDecimal balance,
    String currency
) {
}