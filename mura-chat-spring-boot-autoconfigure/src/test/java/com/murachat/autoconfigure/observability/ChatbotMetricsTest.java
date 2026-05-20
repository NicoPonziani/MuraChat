package com.murachat.autoconfigure.observability;

import com.murachat.autoconfigure.MuraChatAutoConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies that MuraChat registers observability meters when Micrometer is available,
 * and that the LLM request duration timer records correctly.
 */
class ChatbotMetricsTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MuraChatAutoConfiguration.class))
            .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
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
    void chatbotMetricsBean_isRegistered() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(ChatbotMetrics.class);
        });
    }

    @Test
    void llmRequestDurationTimer_isRegisteredInMeterRegistry() {
        contextRunner.run(ctx -> {
            var registry = ctx.getBean(MeterRegistry.class);
            var timer = registry.find("murachat.llm.request.duration").timer();
            assertThat(timer).isNotNull();
            assertThat(timer.getId().getDescription())
                    .contains("LLM response");
        });
    }

    @Test
    void recordLlmRequestDuration_incrementsTimer() {
        contextRunner.run(ctx -> {
            var metrics = ctx.getBean(ChatbotMetrics.class);
            var registry = ctx.getBean(MeterRegistry.class);

            metrics.recordLlmRequestDuration(250);
            metrics.recordLlmRequestDuration(500);

            Timer timer = registry.find("murachat.llm.request.duration").timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(2);
            assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                    .isEqualTo(750.0);
        });
    }

    @Test
    void timeLlmRequest_recordsDurationAndReturnsResult() {
        contextRunner.run(ctx -> {
            var metrics = ctx.getBean(ChatbotMetrics.class);
            var registry = ctx.getBean(MeterRegistry.class);

            String result = metrics.timeLlmRequest(() -> {
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                return "LLM response";
            });

            assertThat(result).isEqualTo("LLM response");

            Timer timer = registry.find("murachat.llm.request.duration").timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
            assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                    .isGreaterThanOrEqualTo(10);
        });
    }

    @Test
    void metricsNotRegistered_whenNoMeterRegistryBean() {
        // Without a MeterRegistry bean the ChatbotMetrics bean cannot be created
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MuraChatAutoConfiguration.class))
                .withBean(ChatClient.Builder.class, () -> mock(ChatClient.Builder.class, inv -> {
                    if (inv.getMethod().getReturnType().isAssignableFrom(ChatClient.Builder.class)) {
                        return inv.getMock();
                    }
                    if (inv.getMethod().getReturnType().isAssignableFrom(ChatClient.class)) {
                        return mock(ChatClient.class);
                    }
                    return null;
                }))
                .run(ctx -> {
                    // Context still starts — metrics are optional
                    assertThat(ctx).doesNotHaveBean(ChatbotMetrics.class);
                });
    }
}


