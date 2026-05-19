package com.murachat.core.port.out;

import com.murachat.core.model.ClassificationResult;

/**
 * Secondary port — classifies whether a user query is within the application's domain.
 *
 * <p>MuraChat provides a default keyword-based implementation. Consuming apps can
 * replace it by registering a {@code @Bean} of this type — Spring's
 * {@code @ConditionalOnMissingBean} ensures the default is not loaded when
 * an override is present.
 *
 * <p>This is a {@link FunctionalInterface} to allow lambda-based implementations:
 * <pre>{@code
 * @Bean
 * public QueryClassifier myClassifier() {
 *     return query -> query.contains("invoice")
 *         ? ClassificationResult.IN_TOPIC
 *         : ClassificationResult.OFF_TOPIC;
 * }
 * }</pre>
 */
@FunctionalInterface
public interface QueryClassifier {

    /**
     * Determines if the given query is within the application's domain.
     *
     * @param query the raw user query string
     * @return {@link ClassificationResult#IN_TOPIC} if the query should be processed,
     *         {@link ClassificationResult#OFF_TOPIC} if it should be blocked
     */
    ClassificationResult classify(String query);
}
