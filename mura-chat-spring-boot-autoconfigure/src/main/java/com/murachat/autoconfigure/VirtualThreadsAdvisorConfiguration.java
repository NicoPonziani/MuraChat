package com.murachat.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * Advisory configuration that checks virtual thread activation at startup.
 *
 * <p>MuraChat workloads are I/O-bound (LLM calls, vector store retrieval, DB indexing).
 * Virtual threads handle thousands of concurrent conversations with minimal overhead.
 * This component logs a recommendation if virtual threads are not enabled.
 *
 * <p>The consuming app enables virtual threads with:
 * <pre>{@code
 * spring:
 *   threads:
 *     virtual:
 *       enabled: true
 * }</pre>
 */
@Configuration(proxyBeanMethods = false)
class VirtualThreadsAdvisorConfiguration {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadsAdvisorConfiguration.class);
    private static final String VIRTUAL_THREADS_PROPERTY = "spring.threads.virtual.enabled";

    private final Environment environment;

    VirtualThreadsAdvisorConfiguration(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    void checkVirtualThreadSupport() {
        boolean enabled = environment.getProperty(VIRTUAL_THREADS_PROPERTY, Boolean.class, false);
        if (!enabled) {
            log.warn("[MuraChat] Virtual threads are NOT enabled. "
                    + "MuraChat workloads are I/O-bound — virtual threads are strongly recommended "
                    + "for optimal concurrency. Enable with: spring.threads.virtual.enabled=true");
        } else {
            log.info("[MuraChat] Virtual threads enabled — optimal for I/O-bound chatbot workloads.");
        }
    }
}


