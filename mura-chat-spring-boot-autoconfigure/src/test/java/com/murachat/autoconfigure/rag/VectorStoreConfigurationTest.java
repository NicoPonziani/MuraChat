package com.murachat.autoconfigure.rag;

import com.murachat.autoconfigure.MuraChatAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies VectorStore auto-configuration:
 * - SearchRequest template uses MuraChat properties (similarityThreshold, topK)
 * - Configuration is conditional on VectorStore bean presence
 * - Different vector store types are handled transparently
 */
class VectorStoreConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MuraChatAutoConfiguration.class))
            .withBean(ChatClient.Builder.class, () -> mock(ChatClient.Builder.class, inv -> {
                if (inv.getMethod().getReturnType().isAssignableFrom(ChatClient.Builder.class)) {
                    return inv.getMock();
                }
                if (inv.getMethod().getReturnType().isAssignableFrom(ChatClient.class)) {
                    return mock(ChatClient.class);
                }
                return null;
            }));

    @Test
    void searchRequestTemplate_usesDefaultProperties() {
        contextRunner
                .withBean(VectorStore.class, () -> mock(VectorStore.class))
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(SearchRequest.class);
                    var searchRequest = ctx.getBean(SearchRequest.class);
                    assertThat(searchRequest.getTopK()).isEqualTo(5);
                    assertThat(searchRequest.getSimilarityThreshold()).isEqualTo(0.75);
                });
    }

    @Test
    void searchRequestTemplate_usesCustomProperties() {
        contextRunner
                .withBean(VectorStore.class, () -> mock(VectorStore.class))
                .withPropertyValues(
                        "murachat.vector-store.type=redis",
                        "murachat.vector-store.similarity-threshold=0.85",
                        "murachat.vector-store.top-k=10"
                )
                .run(ctx -> {
                    var searchRequest = ctx.getBean(SearchRequest.class);
                    assertThat(searchRequest.getTopK()).isEqualTo(10);
                    assertThat(searchRequest.getSimilarityThreshold()).isEqualTo(0.85);
                });
    }

    @Test
    void vectorStoreConfiguration_skippedWhenNoVectorStoreBean() {
        contextRunner
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(SearchRequest.class);
                    // Context starts successfully without VectorStore
                    assertThat(ctx).hasSingleBean(ChatClient.class);
                });
    }

    @Test
    void customSearchRequest_overridesDefault() {
        var customSearchRequest = SearchRequest.builder()
                .topK(3)
                .similarityThreshold(0.9)
                .build();

        contextRunner
                .withBean(VectorStore.class, () -> mock(VectorStore.class))
                .withBean("muraChatSearchRequest", SearchRequest.class, () -> customSearchRequest)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(SearchRequest.class);
                    var sr = ctx.getBean(SearchRequest.class);
                    assertThat(sr.getTopK()).isEqualTo(3);
                    assertThat(sr.getSimilarityThreshold()).isEqualTo(0.9);
                });
    }

    @Test
    void pgvectorType_recognizedFromProperties() {
        contextRunner
                .withBean(VectorStore.class, () -> mock(VectorStore.class))
                .withPropertyValues("murachat.vector-store.type=pgvector")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(SearchRequest.class);
                });
    }

    @Test
    void redisType_recognizedFromProperties() {
        contextRunner
                .withBean(VectorStore.class, () -> mock(VectorStore.class))
                .withPropertyValues("murachat.vector-store.type=redis")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(SearchRequest.class);
                });
    }

    @Test
    void mongodbType_recognizedFromProperties() {
        contextRunner
                .withBean(VectorStore.class, () -> mock(VectorStore.class))
                .withPropertyValues("murachat.vector-store.type=mongodb")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(SearchRequest.class);
                });
    }
}

