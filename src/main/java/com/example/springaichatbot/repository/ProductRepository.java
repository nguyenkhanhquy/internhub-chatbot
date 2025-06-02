package com.example.springaichatbot.repository;

import com.example.springaichatbot.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Tìm sản phẩm theo category ID
    List<Product> findByCategoryCategoryId(Integer categoryId);
    
    // Tìm sản phẩm theo tên category
    List<Product> findByCategoryName(String categoryName);
    
    // Tìm sản phẩm theo khoảng giá
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    
    // Tìm sản phẩm theo tên (chứa từ khóa)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Tìm sản phẩm theo mô tả (chứa từ khóa)
    List<Product> findByDescriptionContainingIgnoreCase(String description);
    
    // Query custom để lấy sản phẩm kèm thông tin category
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.categoryId = :categoryId")
    List<Product> findProductsWithCategoryById(@Param("categoryId") Integer categoryId);
    
    // Query để lấy tất cả sản phẩm kèm category
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category")
    List<Product> findAllWithCategory();
}
