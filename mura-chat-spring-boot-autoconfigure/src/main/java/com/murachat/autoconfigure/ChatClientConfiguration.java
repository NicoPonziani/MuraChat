package com.murachat.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configures the {@link ChatClient} bean for MuraChat.
 *
 * <p>Provider switching is entirely transparent: whichever Spring AI model starter
 * the consuming app includes on classpath (OpenAI, Ollama, etc.), Spring AI
 * auto-configures the corresponding {@code ChatModel}. This configuration builds
 * a {@code ChatClient} on top of the available {@code ChatClient.Builder}
 * (itself auto-configured by {@code spring-ai-autoconfigure-model-chat-client}).
 *
 * <p>The system prompt is loaded from the resource path specified in
 * {@code murachat.rag.system-prompt-template} (default: {@code classpath:murachat/default-system-prompt.txt}).
 */
@Configuration(proxyBeanMethods = false)
class ChatClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ChatClientConfiguration.class);

    /**
     * Builds a pre-configured {@link ChatClient} with the MuraChat system prompt.
     *
     * <p>The consuming app can override this bean entirely by registering its own
     * {@code ChatClient} {@code @Bean}.
     *
     * @param builder    auto-configured by Spring AI based on the available ChatModel
     * @param properties MuraChat configuration properties
     * @param resourceLoader Spring resource loader for resolving the system prompt template
     * @return a ChatClient configured with MuraChat's domain-bound system prompt
     */
    @Bean
    @ConditionalOnMissingBean(ChatClient.class)
    ChatClient muraChatClient(
            ChatClient.Builder builder,
            ChatbotProperties properties,
            ResourceLoader resourceLoader) {

        String systemPrompt = loadSystemPrompt(properties.rag().systemPromptTemplate(), resourceLoader);

        log.info("[MuraChat] ChatClient configured with provider={}, model={}",
                properties.llm().provider(), properties.llm().model());

        return builder
                .defaultSystem(systemPrompt)
                .build();
    }

    private String loadSystemPrompt(String templatePath, ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource(templatePath);
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("[MuraChat] Could not load system prompt from '{}', using built-in fallback.", templatePath);
            return "You are a helpful assistant. Answer only based on the provided context.";
        }
    }
}

