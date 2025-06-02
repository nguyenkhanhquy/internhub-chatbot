package com.example.springaichatbot.service;

import com.example.springaichatbot.entity.Product;
import com.example.springaichatbot.repository.ProductRepository;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MySQLToChromaService {

    private final ChromaVectorStore chromaVectorStore;
    private final ProductRepository productRepository;

    public MySQLToChromaService(ChromaVectorStore chromaVectorStore, ProductRepository productRepository) {
        this.chromaVectorStore = chromaVectorStore;
        this.productRepository = productRepository;
    }

    public void processDataFromMySQL() {
        try {
            log.info("Bắt đầu xử lý dữ liệu từ MySQL...");
            
            // 1. Đọc dữ liệu từ MySQL kèm category để tránh N+1 query problem
            List<Product> data = productRepository.findAllWithCategory();
            log.info("Đã đọc {} sản phẩm từ MySQL", data.size());

            // 2. Chuyển đổi thành Document với metadata đầy đủ
            List<Document> documents = data.stream()
                    .map(entity -> {
                        Map<String, Object> metadata = new HashMap<>();
                        
                        // Metadata cho sản phẩm
                        metadata.put("product_id", entity.getProductId());
                        metadata.put("product_name", entity.getName());
                        metadata.put("product_title", entity.getTitle());
                        metadata.put("product_price", entity.getPrice());
                        metadata.put("product_size", entity.getSize());
                        metadata.put("product_image", entity.getImage());
                        
                        // Metadata cho danh mục (nếu có)
                        String categoryName = "Không có danh mục";
                        Integer categoryId = null;
                        if (entity.getCategory() != null) {
                            categoryName = entity.getCategory().getName();
                            categoryId = entity.getCategory().getCategoryId();
                            metadata.put("category_id", categoryId);
                            metadata.put("category_name", categoryName);
                        }
                        
                        // Tạo content phong phú bao gồm tất cả thông tin
                        StringBuilder contentBuilder = new StringBuilder();
                        contentBuilder.append("=== THÔNG TIN SẢN PHẨM ===\n");
                        contentBuilder.append("Tên sản phẩm: ").append(entity.getName()).append("\n");
                        
                        if (entity.getTitle() != null && !entity.getTitle().trim().isEmpty()) {
                            contentBuilder.append("Tiêu đề: ").append(entity.getTitle()).append("\n");
                        }
                        
                        contentBuilder.append("Giá: ").append(String.format("%.2f", entity.getPrice())).append(" VND\n");
                        
                        if (entity.getSize() != null && !entity.getSize().trim().isEmpty()) {
                            contentBuilder.append("Kích thước: ").append(entity.getSize()).append("\n");
                        }
                        
                        contentBuilder.append("Danh mục: ").append(categoryName).append("\n");
                        
                        if (entity.getDescription() != null && !entity.getDescription().trim().isEmpty()) {
                            contentBuilder.append("\n=== MÔ TẢ SẢN PHẨM ===\n");
                            contentBuilder.append(entity.getDescription()).append("\n");
                        }
                        
                        // Thêm thông tin tìm kiếm
                        contentBuilder.append("\n=== TỪ KHÓA TÌM KIẾM ===\n");
                        contentBuilder.append("Sản phẩm: ").append(entity.getName()).append(", ");
                        contentBuilder.append("Danh mục: ").append(categoryName).append(", ");
                        contentBuilder.append("Giá: ").append(String.format("%.0f", entity.getPrice())).append(" VND");
                        
                        String content = contentBuilder.toString();
                        
                        return new Document(content, metadata);
                    })
                    .toList();

            // 3. Split text nếu cần
            var splitter = new TokenTextSplitter(800, 200, 20, 3000, true);
            documents = splitter.apply(documents);
            log.info("Đã chia thành {} document chunks", documents.size());

            // 4. Embedding và lưu vào ChromaDB
            chromaVectorStore.accept(documents);
            log.info("Đã lưu thành công {} sản phẩm vào ChromaDB", data.size());
            
        } catch (Exception ex) {
            log.error("Lỗi khi xử lý dữ liệu từ MySQL: {}", ex.getMessage(), ex);
            throw new RuntimeException("Không thể xử lý dữ liệu từ MySQL", ex);
        }
    }
    
    // Phương thức để xử lý dữ liệu theo danh mục cụ thể
    public void processDataByCategoryFromMySQL(Integer categoryId) {
        try {
            log.info("Bắt đầu xử lý dữ liệu theo danh mục {} từ MySQL...", categoryId);
            
            // Tìm sản phẩm theo category sử dụng repository method mới
            List<Product> data = productRepository.findProductsWithCategoryById(categoryId);
            
            log.info("Đã đọc {} sản phẩm thuộc danh mục {} từ MySQL", data.size(), categoryId);
            
            if (data.isEmpty()) {
                log.warn("Không có sản phẩm nào thuộc danh mục {}", categoryId);
                return;
            }
            
            // Xử lý tương tự như trên nhưng chỉ cho danh mục cụ thể
            List<Document> documents = data.stream()
                    .map(entity -> {
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("product_id", entity.getProductId());
                        metadata.put("product_name", entity.getName());
                        metadata.put("category_id", entity.getCategory().getCategoryId());
                        metadata.put("category_name", entity.getCategory().getName());
                        metadata.put("product_price", entity.getPrice());

                        String content = String.format(
                            "Sản phẩm: %s%nDanh mục: %s%nGiá: %.2f VND%nMô tả: %s",
                            entity.getName(),
                            entity.getCategory().getName(),
                            entity.getPrice(),
                            entity.getDescription()
                        );

                        return new Document(content, metadata);
                    })
                    .toList();
            
            var splitter = new TokenTextSplitter(800, 200, 20, 3000, true);
            documents = splitter.apply(documents);
            
            chromaVectorStore.accept(documents);
            log.info("Đã lưu thành công {} sản phẩm của danh mục {} vào ChromaDB", data.size(), categoryId);
            
        } catch (Exception ex) {
            log.error("Lỗi khi xử lý dữ liệu danh mục {} từ MySQL: {}", categoryId, ex.getMessage(), ex);
            throw new RuntimeException("Không thể xử lý dữ liệu danh mục từ MySQL", ex);
        }
    }
}
