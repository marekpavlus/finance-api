package com.marek.finance_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAccountDTO(
    @NotBlank(message = "Account name is required.")
    @Size(max = 100, message = "Account name cannot exceed 100 characters.")
    String name
) {
}