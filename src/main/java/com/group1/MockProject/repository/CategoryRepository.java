package com.group1.MockProject.repository;

import com.group1.MockProject.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
  Optional<Category> findCategoryById(int id);
}
