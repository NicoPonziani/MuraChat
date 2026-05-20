package com.murachat.autoconfigure.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for MuraChat observability.
 *
 * <p>Registers {@link ChatbotMetrics} only when Micrometer is on the classpath
 * AND a {@link MeterRegistry} bean is available (i.e., when the consuming app
 * includes {@code spring-boot-starter-actuator}).
 * If the consuming app does NOT include Actuator, this configuration is silently skipped.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean(MeterRegistry.class)
public class ObservabilityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ChatbotMetrics chatbotMetrics(MeterRegistry registry) {
        return new ChatbotMetrics(registry);
    }
}



