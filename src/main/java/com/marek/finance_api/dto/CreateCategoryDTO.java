
package com.marek.finance_api.dto;

import com.marek.finance_api.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCategoryDTO(
    @NotBlank(message = "Category name is required.")
    @Size(max = 100, message = "Category name cannot exceed 100 characters.")
    String name,

    @NotNull(message = "Category type is required.")
    TransactionType type,

    @Size(max = 50, message = "Icon cannot exceed 50 characters.")
    String icon,

    @Pattern(
        regexp = "^#[0-9A-Fa-f]{6}$",
        message = "Color must be a valid HEX color."
    )
    String color
) {}
