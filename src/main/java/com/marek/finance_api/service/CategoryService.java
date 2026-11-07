
package com.marek.finance_api.service;

import com.marek.finance_api.dto.*;
import com.marek.finance_api.exception.ResourceNotFoundException;
import com.marek.finance_api.exception.ResourceConflictException;
import com.marek.finance_api.mapper.CategoryMapper;
import com.marek.finance_api.model.Category;
import com.marek.finance_api.model.TransactionType;
import com.marek.finance_api.repository.CategoryRepository;
import com.marek.finance_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService
{
  private final CategoryRepository categoryRepository;
  private final TransactionRepository transactionRepository;

  public CategoryService(
      CategoryRepository categoryRepository,
      TransactionRepository transactionRepository)
  {
    this.categoryRepository = categoryRepository;
    this.transactionRepository = transactionRepository;
  }

  @Transactional
  public CategoryDTO createCategory(CreateCategoryDTO dto)
  {
    if (categoryRepository.existsByNameIgnoreCaseAndType(
        dto.name().trim(), dto.type()))
    {
      throw new ResourceConflictException(
          "Category already exists.");
    }

    Category category = CategoryMapper.toEntity(dto);
    category.setName(dto.name().trim());

    return toDTO(categoryRepository.save(category));
  }

  public List<CategoryDTO> getAllCategories()
  {
    return categoryRepository.findAll()
        .stream()
        .map(CategoryService::toDTO)
        .toList();
  }

  public List<CategoryDTO> getCategoriesByType(TransactionType type)
  {
    return categoryRepository.findByType(type)
        .stream()
        .map(CategoryService::toDTO)
        .toList();
  }

  public CategoryDTO getCategoryById(Long id)
  {
    return toDTO(findCategory(id));
  }

  @Transactional
  public CategoryDTO updateCategory(Long id, UpdateCategoryDTO dto)
  {
    Category category = findCategory(id);

    if (categoryRepository.existsByNameIgnoreCaseAndType(
        dto.name().trim(), category.getType())
        && !category.getName().equalsIgnoreCase(dto.name().trim()))
    {
      throw new ResourceConflictException(
          "Category already exists.");
    }

    CategoryMapper.updateEntity(category, dto);
    category.setName(dto.name().trim());

    return toDTO(categoryRepository.save(category));
  }

  @Transactional
  public void deleteCategory(Long id)
  {
    Category category = findCategory(id);

    if (transactionRepository.existsByCategoryId(id))
    {
      throw new ResourceConflictException(
          "Cannot delete category with existing transactions.");
    }

    categoryRepository.delete(category);
  }

  private Category findCategory(Long id)
  {
    return categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Category with id " + id + " not found."));
  }

  private static CategoryDTO toDTO(Category category)
  {
    return new CategoryDTO(
        category.getId(),
        category.getName(),
        category.getType(),
        category.getIcon(),
        category.getColor()
    );
  }
}
