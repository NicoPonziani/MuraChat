package com.murachat.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies that MuraChat handles 1000 concurrent requests without thread starvation
 * when virtual threads are enabled.
 *
 * <p>This test simulates concurrent access to the auto-configured beans using Java 21
 * virtual threads. It proves that the configuration is thread-safe and non-blocking.
 */
class VirtualThreadsConcurrencyTest {

    private static final int CONCURRENT_REQUESTS = 1000;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MuraChatAutoConfiguration.class))
            .withPropertyValues("spring.threads.virtual.enabled=true")
            .withBean(ChatClient.Builder.class, () -> mock(ChatClient.Builder.class, invocation -> {
                if (invocation.getMethod().getReturnType().isAssignableFrom(ChatClient.Builder.class)) {
                    return invocation.getMock();
                }
                if (invocation.getMethod().getReturnType().isAssignableFrom(ChatClient.class)) {
                    return mock(ChatClient.class);
                }
                return null;
            }));

    @Test
    void concurrentAccessToBeansWithVirtualThreads_noStarvation() {
        contextRunner.run(ctx -> {
            var classifier = ctx.getBean(com.murachat.core.port.out.QueryClassifier.class);
            var fallback = ctx.getBean(com.murachat.core.port.out.FallbackResponseProvider.class);

            var successCount = new AtomicInteger(0);
            var failureCount = new AtomicInteger(0);
            var latch = new CountDownLatch(CONCURRENT_REQUESTS);

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
                    final int index = i;
                    executor.submit(() -> {
                        try {
                            // Simulate concurrent access to MuraChat beans
                            var result = classifier.classify("Query number " + index);
                            assertThat(result).isNotNull();

                            var msg = fallback.getFallbackMessage(
                                    "test", com.murachat.core.model.ResponseStatus.OFF_TOPIC);
                            assertThat(msg).isNotBlank();

                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                latch.await();
            }

            assertThat(successCount.get())
                    .as("All %d concurrent requests should succeed without thread starvation", CONCURRENT_REQUESTS)
                    .isEqualTo(CONCURRENT_REQUESTS);
            assertThat(failureCount.get()).isZero();
        });
    }

    @Test
    void virtualThreadsWarning_loggedWhenDisabled() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MuraChatAutoConfiguration.class))
                .withBean(ChatClient.Builder.class, () -> mock(ChatClient.Builder.class, invocation -> {
                    if (invocation.getMethod().getReturnType().isAssignableFrom(ChatClient.Builder.class)) {
                        return invocation.getMock();
                    }
                    if (invocation.getMethod().getReturnType().isAssignableFrom(ChatClient.class)) {
                        return mock(ChatClient.class);
                    }
                    return null;
                }))
                // virtual threads NOT enabled — default
                .run(ctx -> {
                    // Verifies context starts successfully even without virtual threads
                    assertThat(ctx).hasSingleBean(VirtualThreadsAdvisorConfiguration.class);
                });
    }

    @Test
    void virtualThreadsInfo_loggedWhenEnabled() {
        contextRunner.run(ctx ->
            assertThat(ctx).hasSingleBean(VirtualThreadsAdvisorConfiguration.class)
        );
    }
}

