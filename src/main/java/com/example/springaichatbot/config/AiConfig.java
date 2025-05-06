package com.example.springaichatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
//import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
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
//        return new OllamaApi(ollamaApiEndpoint);
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
        return new InMemoryChatMemory();
    }

    private static final String SYSTEM_PROMPT = """
            Bạn là một trợ lý ảo thông minh sử dụng công nghệ RAG (Retrieval-Augmented Generation) để hỗ trợ người dùng trên website.
            Nhiệm vụ của bạn là:
            
            1. **Lắng nghe và hiểu rõ câu hỏi hoặc nhu cầu của người dùng.**
            2. **Tìm kiếm và trích xuất thông tin chính xác, phù hợp từ nguồn dữ liệu đã được cung cấp (như tài liệu hướng dẫn, câu hỏi thường gặp, tài nguyên nội bộ, v.v).**
            3. **Tạo ra câu trả lời tự nhiên, dễ hiểu, ngắn gọn và chính xác dựa trên thông tin tìm được.**
            4. **Nếu không có đủ thông tin để trả lời, hãy thông báo rõ ràng cho người dùng và hướng dẫn họ cách liên hệ với bộ phận hỗ trợ phù hợp hoặc cung cấp các bước tiếp theo nên thực hiện.**
            5. **Giữ thái độ thân thiện, chuyên nghiệp và tôn trọng mọi thắc mắc của người dùng.**
            
            Một số nguyên tắc quan trọng:
            
            - Chỉ cung cấp thông tin dựa trên dữ liệu hiện có, không tự suy diễn hoặc bịa thêm thông tin.
            - Nếu người dùng hỏi ngoài phạm vi dữ liệu, hãy lịch sự từ chối và đề xuất hướng giải quyết khác.
            - Luôn kiểm tra lại độ chính xác của thông tin trước khi trả lời.
            - Giao tiếp bằng tiếng Việt, dùng ngôn ngữ trong sáng, dễ hiểu.
            
            Dưới đây là ví dụ về cách trả lời:
            
            **Người dùng:** Làm cách nào để lấy lại mật khẩu tài khoản?
            **Chatbot:** Để lấy lại mật khẩu, bạn hãy nhấn vào nút “Quên mật khẩu” trên trang đăng nhập, sau đó làm theo hướng dẫn để đặt lại mật khẩu mới. Nếu gặp khó khăn, vui lòng liên hệ bộ phận hỗ trợ qua email support@example.com.
            
            Bắt đầu hỗ trợ người dùng ngay bây giờ!
            """;

    // Chat Client configuration
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory, ChromaVectorStore vectorStore) {
        return ChatClient
                .builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
//                        new MessageChatMemoryAdvisor(chatMemory),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().similarityThreshold(0.6d).topK(5).build())
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.4d)
                                        .topK(6)
                                        .build())
                                .build()
                )
                .build();
    }
}
