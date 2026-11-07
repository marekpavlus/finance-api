
package com.marek.finance_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCategoryDTO(
    @NotBlank(message = "Category name is required.")
    @Size(max = 100, message = "Category name cannot exceed 100 characters.")
    String name,

    @Size(max = 50, message = "Icon cannot exceed 50 characters.")
    String icon,

    @Pattern(
        regexp = "^#[0-9A-Fa-f]{6}$",
        message = "Color must be a valid HEX color."
    )
    String color
) {}
