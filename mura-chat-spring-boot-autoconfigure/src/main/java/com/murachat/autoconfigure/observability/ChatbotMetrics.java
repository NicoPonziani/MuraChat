package com.murachat.autoconfigure.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Centralized metrics registry for MuraChat.
 *
 * <p>Exposes Micrometer meters for LLM call duration, retrieval stats,
 * and domain filter activity. All meters are prefixed with {@code murachat.}.
 *
 * <p>This bean is only registered when Micrometer is on the classpath
 * (i.e., when the consuming app includes {@code spring-boot-starter-actuator}).
 */
public class ChatbotMetrics {

    private static final String PREFIX = "murachat.";

    private final Timer llmRequestDuration;

    public ChatbotMetrics(MeterRegistry registry) {
        this.llmRequestDuration = Timer.builder(PREFIX + "llm.request.duration")
                .description("Total time for an LLM response (including network latency)")
                .tag("component", "chat-client")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    /**
     * Records the duration of an LLM call.
     *
     * @param durationMs duration in milliseconds
     */
    public void recordLlmRequestDuration(long durationMs) {
        llmRequestDuration.record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Times the execution of a supplier (typically an LLM call) and records the duration.
     *
     * @param <T>      the return type
     * @param supplier the operation to time
     * @return the result of the supplier
     */
    public <T> T timeLlmRequest(Supplier<T> supplier) {
        return llmRequestDuration.record(supplier);
    }

    /**
     * Returns the raw Timer for advanced usage (e.g., wrapping reactive pipelines).
     */
    public Timer getLlmRequestDurationTimer() {
        return llmRequestDuration;
    }
}

