package com.murachat.core.model;

/**
 * Immutable response model returned by {@link com.murachat.core.port.in.ChatbotService}.
 *
 * @param content        the chatbot's reply text
 * @param conversationId the conversation identifier (generated if null in the request)
 * @param status         semantic outcome of this interaction
 */
public record ChatResponse(
        String content,
        String conversationId,
        ResponseStatus status
) {

    /** Convenience factory for successful on-topic responses. */
    public static ChatResponse onTopic(String content, String conversationId) {
        return new ChatResponse(content, conversationId, ResponseStatus.ON_TOPIC);
    }

    /** Convenience factory for off-topic blocked responses. */
    public static ChatResponse offTopic(String fallbackMessage, String conversationId) {
        return new ChatResponse(fallbackMessage, conversationId, ResponseStatus.OFF_TOPIC);
    }

    /** Convenience factory for in-topic queries with no matching documents. */
    public static ChatResponse noContext(String fallbackMessage, String conversationId) {
        return new ChatResponse(fallbackMessage, conversationId, ResponseStatus.NO_CONTEXT);
    }

    /** Convenience factory for infrastructure errors. */
    public static ChatResponse error(String errorMessage, String conversationId) {
        return new ChatResponse(errorMessage, conversationId, ResponseStatus.ERROR);
    }

    /** Returns true if this response carries a successful on-topic answer. */
    public boolean isSuccessful() {
        return status == ResponseStatus.ON_TOPIC;
    }
}
