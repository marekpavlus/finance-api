
package com.marek.finance_api.repository;

import com.marek.finance_api.model.Category;
import com.marek.finance_api.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long>
{
  List<Category> findByType(TransactionType type);

  boolean existsByNameIgnoreCaseAndType(String name, TransactionType type);
}
