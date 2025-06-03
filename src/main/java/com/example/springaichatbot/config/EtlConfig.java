package com.example.springaichatbot.config;

import java.io.File;
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

@Configuration
@EnableIntegration
@Slf4j
public class EtlConfig {

    @Value("${etl.file.input-directory}")
    private String inputDirectory;

    @Value("${etl.file.polling-interval}")
    private long pollingRate;

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
                    FileSystemResource fileResource = new FileSystemResource(filePath);
                    if (!fileResource.exists() || !fileResource.isReadable()) {
                        log.error("File {} does not exist or is not readable.", filePath);
                        return;
                    }
                    try {
                        log.info("Processing file: {}", filePath);

                        // Đọc file thành document
                        var tikaDocumentReader = new TikaDocumentReader(fileResource);
                        var documents = tikaDocumentReader.read();

                        var splitter = new TokenTextSplitter(520, 300, 20, 3000, true);
                        documents = splitter.apply(documents);

                        // Đưa vào vector store
                        chromaVectorStore.accept(documents);
                        log.info("Successfully processed and stored file: {}", filePath);
                    } catch (Exception ex) {
                        log.error("Error processing file {}: {}", filePath, ex.getMessage(), ex);
                    }
                }).get();
    }
}
