package com.marek.finance_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record CreateAccountDTO(
    @NotBlank(message = "Account name is required.")
    @Size(max = 100, message = "Account name cannot exceed 100 characters.")
    String name,

    @NotBlank(message = "Currency is required.")
    @Pattern(
        regexp = "EUR|USD|DKK|GBP|CZK|PLN",
        message = "Invalid or unsupported currency."
    )
    String currency
) {
}