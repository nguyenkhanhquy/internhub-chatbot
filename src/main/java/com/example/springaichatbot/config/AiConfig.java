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
        return new ChromaApi(chromaApiEndpoint, restClientBuilder);
    }

    // Ollama Embedding Model configuration
    @Bean
    public OllamaEmbeddingModel embeddingModel(OllamaApi ollamaApi) {
        return OllamaEmbeddingModel
                .builder()
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
        return ChromaVectorStore
                .builder(chromaApi, embeddingModel)
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
            Bạn là trợ lý ảo hỗ trợ người dùng trên website quản lý thực tập Khoa CNTT thuộc trường Đại học Sư phạm Kỹ thuật Thành phố Hồ Chí Minh (HCMUTE).
            
            Nhiệm vụ:
            1. Hiểu rõ câu hỏi người dùng.
            2. Tìm thông tin chính xác từ dữ liệu đã được cung cấp.
            3. Trả lời rõ ràng, ngắn gọn, dễ hiểu.
            4. Nếu thiếu thông tin, hướng dẫn người dùng liên hệ hỗ trợ.
            5. Giao tiếp lịch sự, chuyên nghiệp.
            
            Nguyên tắc:
            - Luôn trả lời bằng ngôn ngữ Việt Nam, lịch sự, dùng ngôn ngữ dễ hiểu.
            - Không suy diễn, chỉ trả lời dựa trên dữ liệu có sẵn.
            - Luôn định dạng câu trả lời rõ ràng, dễ đọc. Tránh ký tự đặc biệt không cần thiết.
            - Nếu câu hỏi nằm ngoài phạm vi, hãy lịch sự từ chối và đề xuất hướng giải quyết khác.
            """;

    // Chat Client configuration
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory, ChromaVectorStore vectorStore) {
        return ChatClient
                .builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.5d)
                                        .topK(5)
                                        .build())
                                .build()
                )
                .build();
    }
}
