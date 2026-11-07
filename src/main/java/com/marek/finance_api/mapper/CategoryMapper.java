
package com.marek.finance_api.mapper;

import com.marek.finance_api.dto.CreateCategoryDTO;
import com.marek.finance_api.dto.UpdateCategoryDTO;
import com.marek.finance_api.model.Category;

public class CategoryMapper
{
  public static Category toEntity(CreateCategoryDTO dto)
  {
    Category category = new Category();
    category.setName(dto.name());
    category.setType(dto.type());
    category.setIcon(dto.icon());
    category.setColor(dto.color());

    return category;
  }

  public static void updateEntity(Category category, UpdateCategoryDTO dto)
  {
    category.setName(dto.name());
    category.setIcon(dto.icon());
    category.setColor(dto.color());
  }
}
