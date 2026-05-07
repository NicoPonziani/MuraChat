package com.murachat.core.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Immutable request model for a single chatbot interaction.
 *
 * <p>The {@code context} map allows the consuming app to pass application-level
 * metadata (e.g., current page, user role) that can be injected into the
 * system prompt for contextual responses.
 *
 * @param message        the user's message — must not be blank
 * @param conversationId identifies the conversation for multi-turn memory;
 *                       {@code null} means stateless (no memory)
 * @param context        optional application metadata passed to the LLM prompt
 */
public record ChatRequest(
        @NotBlank String message,
        String conversationId,
        Map<String, Object> context
) {

    /** Stateless request — no memory, no extra context. */
    public static ChatRequest of(String message) {
        return new ChatRequest(message, null, null);
    }

    /** Multi-turn request — memory identified by conversationId, no extra context. */
    public static ChatRequest of(String message, String conversationId) {
        return new ChatRequest(message, conversationId, null);
    }

    /** Full request with conversation memory and application context. */
    public static ChatRequest of(String message, String conversationId, Map<String, Object> context) {
        return new ChatRequest(message, conversationId, context);
    }

    /** Returns true if this request participates in a named conversation. */
    public boolean hasConversation() {
        return conversationId != null && !conversationId.isBlank();
    }

    /** Returns true if application context metadata is present. */
    public boolean hasContext() {
        return context != null && !context.isEmpty();
    }
}
