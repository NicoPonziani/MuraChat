package com.murachat.core.model;

/**
 * Semantic outcome of a chatbot interaction.
 *
 * <p>Using an enum instead of exceptions for expected behaviors keeps
 * the caller's code clean — off-topic responses are not errors, they are
 * intentional outcomes. Exceptions are reserved for infrastructure failures.
 */
public enum ResponseStatus {

    /** Response generated with relevant context found in the knowledge base. */
    ON_TOPIC,

    /** Query blocked by the domain filter before reaching the LLM. */
    OFF_TOPIC,

    /**
     * Query is in-topic but no relevant documents were found in the vector store
     * above the configured similarity threshold.
     */
    NO_CONTEXT,

    /** Infrastructure failure: LLM unreachable, vector store down, etc. */
    ERROR
}
