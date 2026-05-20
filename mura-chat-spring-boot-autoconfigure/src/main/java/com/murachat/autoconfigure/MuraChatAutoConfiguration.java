package com.murachat.autoconfigure;

import com.murachat.core.model.ClassificationResult;
import com.murachat.core.port.out.FallbackResponseProvider;
import com.murachat.core.port.out.QueryClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Main auto-configuration class for MuraChat.
 *
 * <p>Registers default bean implementations for all secondary ports and imports
 * the {@link ChatClientConfiguration} for LLM provider switching.
 * Every bean is annotated with {@code @ConditionalOnMissingBean} — consuming apps
 * can override any component by simply registering their own {@code @Bean}.
 *
 * <p>Provider switching works transparently: the consuming app includes
 * the desired Spring AI provider starter on its classpath (e.g.
 * {@code spring-ai-starter-model-openai} or {@code spring-ai-starter-model-ollama}).
 * Spring AI auto-configures the corresponding {@code ChatModel}, and MuraChat
 * builds its {@code ChatClient} on top of it — no code changes required.
 */
@AutoConfiguration
@EnableConfigurationProperties(ChatbotProperties.class)
@ConditionalOnProperty(prefix = "murachat", name = "enabled", matchIfMissing = true)
@ConditionalOnClass(ChatClient.class)
@Import({ChatClientConfiguration.class, VirtualThreadsAdvisorConfiguration.class})
public class MuraChatAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MuraChatAutoConfiguration.class);

    /**
     * Default keyword-based query classifier.
     * Passthrough placeholder — returns IN_TOPIC for all queries.
     * Will be replaced in Sprint 4 by {@code KeywordQueryClassifier}
     * with configurable keyword lists.
     */
    @Bean
    @ConditionalOnMissingBean(QueryClassifier.class)
    public QueryClassifier defaultQueryClassifier(ChatbotProperties properties) {
        log.info("[MuraChat] Using default passthrough QueryClassifier. "
                + "Override by registering a QueryClassifier @Bean in your application.");
        return query -> ClassificationResult.IN_TOPIC;
    }

    /**
     * Default fallback response provider.
     *
     * <p>Override by registering a {@code FallbackResponseProvider} bean
     * to customize refusal messages (e.g., localization, app-specific suggestions).
     */
    @Bean
    @ConditionalOnMissingBean(FallbackResponseProvider.class)
    public FallbackResponseProvider defaultFallbackResponseProvider() {
        return (query, reason) -> switch (reason) {
            case OFF_TOPIC  -> "I can only answer questions related to this application.";
            case NO_CONTEXT -> "I don't have enough information to answer that question.";
            case ERROR      -> "Something went wrong. Please try again later.";
            default         -> "I'm unable to process your request at this time.";
        };
    }
}
