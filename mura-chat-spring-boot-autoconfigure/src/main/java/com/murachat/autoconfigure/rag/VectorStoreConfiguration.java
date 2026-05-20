package com.murachat.autoconfigure.rag;

import static com.murachat.autoconfigure.ChatbotProperties.VectorStoreProperties;

import com.murachat.autoconfigure.ChatbotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for VectorStore integration in MuraChat.
 *
 * <p>Spring AI auto-configures the concrete {@link VectorStore} implementation
 * based on which starter is on the consuming app's classpath:
 * <ul>
 *   <li>{@code spring-ai-starter-vector-store-pgvector} → PgVectorStore</li>
 *   <li>{@code spring-ai-starter-vector-store-redis} → RedisVectorStore</li>
 *   <li>{@code spring-ai-starter-vector-store-mongodb-atlas} → MongoDBAtlasVectorStore</li>
 * </ul>
 *
 * <p>This configuration validates that a VectorStore is available, logs which
 * implementation is active, and provides a pre-configured {@link SearchRequest}
 * template with MuraChat's similarity threshold and topK settings.
 *
 * <p>Switching vector stores requires only a classpath change (different Maven
 * dependency) and the corresponding connection properties — no code changes.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(VectorStore.class)
@ConditionalOnBean(VectorStore.class)
public class VectorStoreConfiguration {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfiguration.class);

    /**
     * Provides a pre-configured {@link SearchRequest} template with MuraChat's
     * similarity threshold and topK from properties.
     *
     * <p>This template is used by the RAG pipeline (Sprint 3-T3) when performing
     * similarity searches. The consuming app can override this bean to customize
     * search behavior (e.g., adding metadata filters).
     */
    @Bean
    @ConditionalOnMissingBean(name = "muraChatSearchRequest")
    SearchRequest muraChatSearchRequest(ChatbotProperties properties, VectorStore vectorStore) {
        VectorStoreProperties vsProps = properties.vectorStore();

        log.info("[MuraChat] VectorStore active: {} (type configured: {}, similarityThreshold={}, topK={})",
                vectorStore.getClass().getSimpleName(),
                vsProps.type(),
                vsProps.similarityThreshold(),
                vsProps.topK());

        return SearchRequest.builder()
                .topK(vsProps.topK())
                .similarityThreshold(vsProps.similarityThreshold())
                .build();
    }
}

