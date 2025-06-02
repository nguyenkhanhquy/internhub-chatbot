package com.example.springaichatbot.config;

import java.io.File;

import com.example.springaichatbot.service.MySQLToChromaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource.WatchEventType;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableIntegration
@EnableScheduling
@Slf4j
public class EtlConfig {

    @Value("${etl.file.input-directory}")
    private String inputDirectory;

    @Value("${etl.file.polling-interval}")
    private long pollingRate;

    private final MySQLToChromaService mySQLToChromaService;

    public EtlConfig(MySQLToChromaService mySQLToChromaService) {
        this.mySQLToChromaService = mySQLToChromaService;
    }

    @Bean
    public IntegrationFlow fileReadingFlow() {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(inputDirectory))
                                .regexFilter(".*\\.(txt|pdf|docx)")
                                .useWatchService(true)
                                .watchEvents(WatchEventType.CREATE)
                        , e -> e.poller(Pollers.fixedDelay(pollingRate)))
                .channel(processedFileChannel())
                .get();
    }

    @Bean
    public MessageChannel processedFileChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow processFileFlow(ChromaVectorStore chromaVectorStore) {
        return IntegrationFlow.from(processedFileChannel())
                .handle(message -> {
                    String filePath = message.getPayload().toString();
                    try {
                        log.info("Processing file: {}", filePath);
                        var tikaDocumentReader = new TikaDocumentReader(new FileSystemResource(message.getPayload().toString()));
                        var documents = tikaDocumentReader.read();
                        var splitter = new TokenTextSplitter(520, 300, 20, 3000, true);
                        documents = splitter.apply(documents);
                        chromaVectorStore.accept(documents);
                        log.info("Processed file: {}", filePath);
                    } catch (Exception ex) {
                        log.error("Error processing file {}: {}", filePath, ex.getMessage(), ex);
                    }
                }).get();
    }

    // Tự động đồng bộ dữ liệu từ MySQL sang ChromaDB mỗi 30 phút
    @Scheduled(fixedRate = 1800000) // 30 phút = 30 * 60 * 1000 ms
    public void autoSyncMySQLToChroma() {
        try {
            log.info("Bắt đầu tự động đồng bộ dữ liệu từ MySQL sang ChromaDB...");
            mySQLToChromaService.processDataFromMySQL();
            log.info("Hoàn thành tự động đồng bộ dữ liệu");
        } catch (Exception ex) {
            log.error("Lỗi khi tự động đồng bộ dữ liệu: {}", ex.getMessage(), ex);
        }
    }
}
