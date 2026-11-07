
package com.marek.finance_api.controller;

import tools.jackson.databind.ObjectMapper;
import com.marek.finance_api.dto.CategoryDTO;
import com.marek.finance_api.dto.CreateCategoryDTO;
import com.marek.finance_api.dto.UpdateCategoryDTO;
import com.marek.finance_api.exception.ResourceConflictException;
import com.marek.finance_api.exception.ResourceNotFoundException;
import com.marek.finance_api.model.TransactionType;
import com.marek.finance_api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CategoryService categoryService;

  private CategoryDTO categoryDTO() {
    return new CategoryDTO(
        1L, "Groceries", TransactionType.EXPENSE,
        "shopping-cart", "#FF5733"
    );
  }

  @Test
  void shouldCreateCategory() throws Exception {
    CreateCategoryDTO dto = new CreateCategoryDTO(
        "Groceries", TransactionType.EXPENSE,
        "shopping-cart", "#FF5733"
    );

    when(categoryService.createCategory(any(CreateCategoryDTO.class)))
        .thenReturn(categoryDTO());

    mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Groceries"))
        .andExpect(jsonPath("$.type").value("EXPENSE"))
        .andExpect(jsonPath("$.icon").value("shopping-cart"))
        .andExpect(jsonPath("$.color").value("#FF5733"));
  }

  @Test
  void shouldReturn400WhenCreatingInvalidCategory() throws Exception {
    String json = """
            {
                "name": "",
                "type": null,
                "icon": "shopping-cart",
                "color": "invalid"
            }
            """;

    mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(categoryService);
  }

  @Test
  void shouldReturnAllCategories() throws Exception {
    when(categoryService.getAllCategories())
        .thenReturn(List.of(categoryDTO()));

    mockMvc.perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Groceries"));
  }

  @Test
  void shouldReturnCategoriesFilteredByType() throws Exception {
    when(categoryService.getCategoriesByType(TransactionType.EXPENSE))
        .thenReturn(List.of(categoryDTO()));

    mockMvc.perform(get("/api/categories")
            .param("type", "EXPENSE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].type").value("EXPENSE"));

    verify(categoryService).getCategoriesByType(TransactionType.EXPENSE);
    verify(categoryService, never()).getAllCategories();
  }

  @Test
  void shouldReturnCategoryById() throws Exception {
    when(categoryService.getCategoryById(1L))
        .thenReturn(categoryDTO());

    mockMvc.perform(get("/api/categories/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Groceries"));
  }

  @Test
  void shouldReturn404WhenCategoryDoesNotExist() throws Exception {
    when(categoryService.getCategoryById(99L))
        .thenThrow(new ResourceNotFoundException("Category not found."));

    mockMvc.perform(get("/api/categories/99"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldUpdateCategory() throws Exception {
    UpdateCategoryDTO dto = new UpdateCategoryDTO(
        "Food", "utensils", "#00FF00"
    );

    CategoryDTO updated = new CategoryDTO(
        1L, "Food", TransactionType.EXPENSE,
        "utensils", "#00FF00"
    );

    when(categoryService.updateCategory(eq(1L), any(UpdateCategoryDTO.class)))
        .thenReturn(updated);

    mockMvc.perform(put("/api/categories/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Food"))
        .andExpect(jsonPath("$.color").value("#00FF00"));
  }

  @Test
  void shouldReturn400WhenUpdatingInvalidCategory() throws Exception {
    String json = """
            {
                "name": "",
                "icon": "utensils",
                "color": "invalid"
            }
            """;

    mockMvc.perform(put("/api/categories/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(categoryService);
  }

  @Test
  void shouldReturn409WhenCategoryNameAlreadyExists() throws Exception {
    CreateCategoryDTO dto = new CreateCategoryDTO(
        "Groceries", TransactionType.EXPENSE,
        "shopping-cart", "#FF5733"
    );

    when(categoryService.createCategory(any(CreateCategoryDTO.class)))
        .thenThrow(new ResourceConflictException("Category already exists."));

    mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldDeleteCategory() throws Exception {
    doNothing().when(categoryService).deleteCategory(1L);

    mockMvc.perform(delete("/api/categories/1"))
        .andExpect(status().isNoContent());

    verify(categoryService).deleteCategory(1L);
  }

  @Test
  void shouldReturn409WhenDeletingCategoryWithTransactions() throws Exception {
    doThrow(new ResourceConflictException(
        "Cannot delete category with existing transactions."
    )).when(categoryService).deleteCategory(1L);

    mockMvc.perform(delete("/api/categories/1"))
        .andExpect(status().isConflict());
  }
}
