
package com.marek.finance_api.controller;

import com.marek.finance_api.dto.CategoryDTO;
import com.marek.finance_api.dto.CreateCategoryDTO;
import com.marek.finance_api.dto.UpdateCategoryDTO;
import com.marek.finance_api.model.TransactionType;
import com.marek.finance_api.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Endpoints for managing transaction categories")
public class CategoryController {

  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @PostMapping
  @Operation(summary = "Create category")
  public ResponseEntity<CategoryDTO> createCategory(
      @Valid @RequestBody CreateCategoryDTO dto) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(categoryService.createCategory(dto));
  }

  @GetMapping
  @Operation(summary = "Get all categories or filter by type")
  public ResponseEntity<List<CategoryDTO>> getCategories(
      @RequestParam(required = false) TransactionType type) {

    List<CategoryDTO> categories = type == null
        ? categoryService.getAllCategories()
        : categoryService.getCategoriesByType(type);

    return ResponseEntity.ok(categories);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID")
  public ResponseEntity<CategoryDTO> getCategoryById(
      @PathVariable Long id) {

    return ResponseEntity.ok(categoryService.getCategoryById(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update category")
  public ResponseEntity<CategoryDTO> updateCategory(
      @PathVariable Long id,
      @Valid @RequestBody UpdateCategoryDTO dto) {

    return ResponseEntity.ok(categoryService.updateCategory(id, dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete category")
  public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {

    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}
