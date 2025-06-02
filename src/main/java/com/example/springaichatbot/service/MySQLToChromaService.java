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
import java.util.stream.Collectors;

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
            
            // 1. Đọc dữ liệu từ MySQL
            List<Product> data = productRepository.findAll();
            log.info("Đã đọc {} sản phẩm từ MySQL", data.size());

            // 2. Chuyển đổi thành Document với metadata
            List<Document> documents = data.stream()
                    .map(entity -> {
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("product_id", entity.getProductId());
                        metadata.put("name", entity.getName());
                        
                        // Tạo content bao gồm tất cả thông tin quan trọng
                        String content = String.format(
                            "Tên sản phẩm: %s%nTiêu đề: %s%nGiá: %.2f%nMô tả: %s",
                            entity.getName(),
                            entity.getTitle(),
                            entity.getPrice(),
                            entity.getDescription()
                        );
                        
                        return new Document(content, metadata);
                    })
                    .toList();

            // 3. Split text nếu cần
            var splitter = new TokenTextSplitter(520, 300, 20, 3000, true);
            documents = splitter.apply(documents);
            log.info("Đã chia thành {} document chunks", documents.size());

            // 4. Embedding và lưu vào ChromaDB
            chromaVectorStore.accept(documents);
            log.info("Đã lưu thành công vào ChromaDB");
            
        } catch (Exception ex) {
            log.error("Lỗi khi xử lý dữ liệu từ MySQL: {}", ex.getMessage(), ex);
            throw new RuntimeException("Không thể xử lý dữ liệu từ MySQL", ex);
        }
    }
}
