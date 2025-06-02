package com.example.springaichatbot.repository;

import com.example.springaichatbot.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Tìm category theo tên
    Category findByName(String name);
    
    // Tìm category theo tên (chứa từ khóa)
    List<Category> findByNameContainingIgnoreCase(String name);
    
    // Query để lấy category cùng số lượng sản phẩm
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN Product p ON c.categoryId = p.category.categoryId GROUP BY c")
    List<Object[]> findCategoriesWithProductCount();
} 