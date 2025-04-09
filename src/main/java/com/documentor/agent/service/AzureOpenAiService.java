package com.documentor.agent.service;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for configuring and providing access to Azure OpenAI LLM.
 */
public class AzureOpenAiService {
    private static final Logger logger = LoggerFactory.getLogger(AzureOpenAiService.class);
    
    private final Dotenv dotenv;
    private ChatLanguageModel chatModel;
    
    public AzureOpenAiService(Dotenv dotenv) {
        this.dotenv = dotenv;
        initialize();
    }
    
    /**
     * Initializes the Azure OpenAI client using credentials from .env file.
     */
    private void initialize() {
        try {
            logger.info("Initializing Azure OpenAI client");
            
            String endpoint = dotenv.get("AZURE_OPENAI_ENDPOINT");
            String apiKey = dotenv.get("AZURE_OPENAI_API_KEY");
            String deploymentId = dotenv.get("AZURE_OPENAI_DEPLOYMENT_ID");
            String apiVersion = dotenv.get("AZURE_OPENAI_API_VERSION");
              if (endpoint == null || apiKey == null || deploymentId == null || apiVersion == null) {
                throw new IllegalStateException(
                    "Missing required Azure OpenAI configuration. Please ensure your .env file contains: " +
                    "AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY, AZURE_OPENAI_DEPLOYMENT_ID, and AZURE_OPENAI_API_VERSION"
                );
            }
            // In LangChain4j 0.27.1, there's no direct method to set API version in the builder
            // We need to construct the Azure OpenAI chat model differently
            chatModel = AzureOpenAiChatModel.builder()
                .endpoint(endpoint)
                .apiKey(apiKey)
                .deploymentName(deploymentId)
                // API version is not directly settable in this version, removing the method call
                .temperature(0.1) // Low temperature for more precise/deterministic outputs
                .maxTokens(4000)  // Adjust based on model capabilities
                .build();
            
            logger.info("Azure OpenAI client initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Azure OpenAI client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Azure OpenAI service", e);
        }
    }
    
    /**
     * Gets the configured ChatLanguageModel instance.
     *
     * @return ChatLanguageModel instance
     */
    public ChatLanguageModel getChatModel() {
        return chatModel;
    }
}
