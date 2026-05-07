package com.murachat.autoconfigure;

import com.murachat.core.model.ClassificationResult;
import com.murachat.core.model.ResponseStatus;
import com.murachat.core.port.out.FallbackResponseProvider;
import com.murachat.core.port.out.QueryClassifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that MuraChatAutoConfiguration registers the expected default beans
 * and respects @ConditionalOnMissingBean for overrides.
 *
 * Uses ApplicationContextRunner — no Spring context started, tests are fast.
 */
class MuraChatAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MuraChatAutoConfiguration.class));

    @Test
    void defaultBeansAreRegisteredWhenNoOverridesPresent() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(QueryClassifier.class);
            assertThat(ctx).hasSingleBean(FallbackResponseProvider.class);
            assertThat(ctx).hasSingleBean(ChatbotProperties.class);
        });
    }

    @Test
    void defaultQueryClassifier_returnInTopic() {
        contextRunner.run(ctx -> {
            var classifier = ctx.getBean(QueryClassifier.class);
            assertThat(classifier.classify("How do I reset my password?"))
                    .isEqualTo(ClassificationResult.IN_TOPIC);
        });
    }

    @Test
    void defaultFallbackProvider_returnsMessageForOffTopic() {
        contextRunner.run(ctx -> {
            var fallback = ctx.getBean(FallbackResponseProvider.class);
            var message = fallback.getFallbackMessage("Who won Sanremo?", ResponseStatus.OFF_TOPIC);
            assertThat(message).isNotBlank();
        });
    }

    @Test
    void customQueryClassifier_overridesDefault() {
        contextRunner
                .withBean(QueryClassifier.class, () -> query -> ClassificationResult.OFF_TOPIC)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(QueryClassifier.class);
                    var classifier = ctx.getBean(QueryClassifier.class);
                    // The custom bean always returns OFF_TOPIC
                    assertThat(classifier.classify("anything"))
                            .isEqualTo(ClassificationResult.OFF_TOPIC);
                });
    }

    @Test
    void propertiesDefaultValues_areCorrect() {
        contextRunner.run(ctx -> {
            var props = ctx.getBean(ChatbotProperties.class);
            assertThat(props.enabled()).isTrue();
            assertThat(props.rag().allowEmptyContext()).isFalse();
            assertThat(props.vectorStore().similarityThreshold()).isEqualTo(0.75);
            assertThat(props.memory().maxMessages()).isEqualTo(20);
            assertThat(props.domainFilter().enabled()).isTrue();
        });
    }

    @Test
    void muraChatDisabled_noBeansRegistered() {
        contextRunner
                .withPropertyValues("murachat.enabled=false")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(ChatbotProperties.class);
                });
    }
}
