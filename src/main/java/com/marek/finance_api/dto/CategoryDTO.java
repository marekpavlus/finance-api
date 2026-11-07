
package com.marek.finance_api.dto;

import com.marek.finance_api.model.TransactionType;

public record CategoryDTO(
    Long id,
    String name,
    TransactionType type,
    String icon,
    String color
) {}
