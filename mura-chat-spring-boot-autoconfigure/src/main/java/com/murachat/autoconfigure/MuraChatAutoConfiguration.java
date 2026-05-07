package com.murachat.autoconfigure;

import com.murachat.core.model.ResponseStatus;
import com.murachat.core.port.out.FallbackResponseProvider;
import com.murachat.core.port.out.QueryClassifier;
import com.murachat.core.model.ClassificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Main auto-configuration class for MuraChat.
 *
 * <p>Registers default bean implementations for all secondary ports.
 * Every bean is annotated with {@code @ConditionalOnMissingBean} — consuming apps
 * can override any component by simply registering their own {@code @Bean}.
 *
 * <p><strong>Sprint roadmap:</strong>
 * <ul>
 *   <li>Sprint 1 (current): scaffold, properties, default port implementations</li>
 *   <li>Sprint 2: ChatClient auto-configuration with provider switching</li>
 *   <li>Sprint 3: RAG pipeline (VectorStore, RetrievalAugmentationAdvisor)</li>
 *   <li>Sprint 4: DomainFilterAdvisor, ChatMemory, ChatbotServiceImpl</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties(ChatbotProperties.class)
public class MuraChatAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MuraChatAutoConfiguration.class);

    /**
     * Default keyword-based query classifier.
     *
     * <p>Sprint 1 placeholder — returns IN_TOPIC for all queries.
     * Will be replaced in Sprint 4 by {@code KeywordQueryClassifier}
     * with configurable keyword lists.
     */
    @Bean
    @ConditionalOnMissingBean(QueryClassifier.class)
    public QueryClassifier defaultQueryClassifier(ChatbotProperties properties) {
        log.info("""
                [MuraChat] Using default passthrough QueryClassifier.
                "Override by registering a QueryClassifier @Bean in your application.
        """);
        // Sprint 1: passthrough — classificazione reale in Sprint 4 (DomainFilterAdvisor)
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
