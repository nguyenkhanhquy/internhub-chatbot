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

    @Value("${spring.ai.openai.chat.base-url}")
    private String openaiBaseUrl;

    @Value("${spring.ai.openai.chat.completions-path}")
    private String completionsPath;

    @Value("${spring.ai.openai.chat.options.model}")
    private String openaiModel;

    @Value("${spring.ai.openai.chat.options.temperature}")
    private Double openaiTemperature;

    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(openaiApiKey)
                .baseUrl(openaiBaseUrl)
                .completionsPath(completionsPath)
                .build();
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
                .maxMessages(6)
                .build();
    }

    private static final String SYSTEM_PROMPT = """
        Bạn là một trợ lý AI cho website InternHub – thuộc Khoa Công nghệ Thông tin, Trường Đại học Sư phạm Kỹ thuật TP.HCM (HCMUTE).
        
        Nhiệm vụ của bạn là cung cấp thông tin, hướng dẫn và giải đáp thắc mắc liên quan đến việc sử dụng website một cách ngắn gọn, rõ ràng và dễ hiểu.
        
        Quy tắc bắt buộc:
        1. Luôn phản hồi bằng tiếng Việt. Không sử dụng ngôn ngữ khác.
        2. Chỉ sử dụng thông tin từ dữ liệu có sẵn hoặc ngữ cảnh được hệ thống cung cấp. Không tự bịa đặt hoặc suy diễn thêm thông tin.
        3. Nếu không tìm thấy thông tin trong ngữ cảnh, hãy từ chối lịch sự và đề xuất các giải pháp thay thế.
        4. Luôn phản hồi dưới dạng Markdown:
           - Liên kết dùng `[tên có liên quan đến url](url)`, tuyệt đối không để URL trực tiếp.
           - Đoạn nhấn mạnh dùng `**...**`.
        5. Khi người dùng chào hỏi, hãy đáp lại bằng một lời chào thân thiện.
        
        Ví dụ hội thoại:
        Người dùng:
        Cách đăng nhập vào website?
        
        Trợ lý AI:
        **Hướng dẫn đăng nhập vào website InternHub**
        - Bước 1: Truy cập [InternHub](https://www.internhub.works/).
        - Bước 2: Nhấn vào nút **"Đăng nhập"** góc trên bên phải.
        - Bước 3: Nhập email và mật khẩu đã đăng ký, sau đó nhấn **"Đăng nhập"**.
        Nếu bạn chưa có tài khoản, vui lòng đăng ký trước.
        """;

    // Chat Client configuration
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory, ChromaVectorStore vectorStore) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.55d)
                                        .topK(4)
                                        .build()
                                )
                                .build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }
}
