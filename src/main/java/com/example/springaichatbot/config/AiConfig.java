package com.example.springaichatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig {

    // Ollama API configuration
    @Value("${ollama.model}")
    private String ollamaModel;

    @Value("${ollama.api.endpoint}")
    private String ollamaApiEndpoint;

    @Bean
    public OllamaApi ollamaApi() {
        return OllamaApi.builder().baseUrl(ollamaApiEndpoint).build();
    }

    @Bean
    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(
                        OllamaOptions
                                .builder()
                                .model(ollamaModel)
                                .temperature(0.5)
                                .build())
                .build();
    }

    // OpenAI API configuration
    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String openaiBaseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String openaiModel;

    @Value("${spring.ai.openai.chat.options.temperature}")
    private Double openaiTemperature;

    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder().apiKey(openaiApiKey).baseUrl(openaiBaseUrl).build();
    }

    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(
                        OpenAiChatOptions
                                .builder()
                                .model(openaiModel)
                                .temperature(openaiTemperature)
                                .build()
                )
                .build();
    }

    // Chroma API configuration
    @Value("${chroma.api.endpoint}")
    private String chromaApiEndpoint;

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory());
    }

    @Bean
    public ChromaApi chromaApi(RestClient.Builder restClientBuilder) {
        return ChromaApi.builder()
                .baseUrl(chromaApiEndpoint)
                .restClientBuilder(restClientBuilder)
                .build();
    }

    // Ollama Embedding Model configuration
    @Bean
    public OllamaEmbeddingModel embeddingModel(OllamaApi ollamaApi) {
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(
                        OllamaOptions
                                .builder()
                                .model(OllamaModel.NOMIC_EMBED_TEXT)
                                .build()
                )
                .build();
    }

    // Chroma Vector Store configuration
    @Bean
    public ChromaVectorStore chromaVectorStore(ChromaApi chromaApi, EmbeddingModel embeddingModel) {
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName("rag")
                .initializeSchema(true)
                .build();
    }

    // Chat Memory configuration
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(15)
                .build();
    }

    private static final String SYSTEM_PROMPT = """
            Bạn là một trợ lý AI thông minh, được phát triển để hỗ trợ người dùng của website quản lý thực tập của Khoa Công nghệ Thông tin, Trường Đại học Sư phạm Kỹ thuật TP.HCM (HCMUTE).
        
            Nhiệm vụ của bạn bao gồm:
            1. Tiếp nhận và hiểu rõ câu hỏi hoặc nhu cầu của người dùng.
            2. Tìm kiếm và trích xuất thông tin chính xác, phù hợp từ nguồn dữ liệu đã được cung cấp.
            3. Tạo ra câu trả lời ngắn gọn, dễ hiểu, đúng trọng tâm và thân thiện.
            4. Nếu không có đủ thông tin, hãy thông báo rõ ràng và đề xuất hướng xử lý phù hợp.
            5. Luôn giữ thái độ chuyên nghiệp, tôn trọng và hỗ trợ tận tình.
        
            Một số nguyên tắc quan trọng:
            - Chỉ cung cấp thông tin dựa trên dữ liệu hiện có, không tự suy diễn hoặc bịa thêm thông tin.
            - Khi câu hỏi của người dùng vượt ngoài phạm vi dữ liệu, hãy từ chối lịch sự và gợi ý giải pháp khác.
            - Luôn kiểm tra lại độ chính xác của thông tin trước khi trả lời.
            - Trả lời bằng tiếng Việt Nam, ngắn gọn và dễ hiểu.
            
            Ngữ cảnh sử dụng và dữ liệu hiện có:
            """;


    // Chat Client configuration
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory, ChromaVectorStore vectorStore) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.5d)
                                        .topK(5)
                                        .build()
                                )
                                .build()
                )
                .build();
    }
}
