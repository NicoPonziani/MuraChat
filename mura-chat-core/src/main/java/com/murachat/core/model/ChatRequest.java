package com.murachat.core.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable request model for a single chatbot interaction.
 *
 * <p>The {@code conversationId} is always guaranteed to be non-null after construction.
 * If not provided by the consuming app, it is auto-generated via {@link UUID#randomUUID()}.
 * This ensures that {@code ChatMemory}, {@code RateLimitingAdvisor}, and structured logging
 * are always correlatable without requiring explicit conversation management from the app.
 *
 * <p>The {@code context} map allows the consuming app to pass application-level
 * metadata (e.g., current page, user role) that can be injected into the system prompt.
 * MuraChat is security-agnostic — it never reads the {@code SecurityContext}. Any
 * user-specific data must be passed explicitly here.
 *
 * @param message        the user's message — must not be blank
 * @param conversationId identifies the conversation for multi-turn memory;
 *                       auto-generated if {@code null}
 * @param context        optional application metadata passed to the LLM prompt;
 *                       defaults to {@link Map#of()} if {@code null}
 */
public record ChatRequest(
        @NotBlank String message,
        String conversationId,
        Map<String, Object> context
) {

    /**
     * Compact constructor — normalizes null values so every field is always usable.
     * Callers downstream never need to null-check {@code conversationId} or {@code context}.
     */
    public ChatRequest {
        if (conversationId == null) conversationId = UUID.randomUUID().toString();
        if (context == null) context = Map.of();
    }

    /**
     * Creates a request with an auto-generated {@code conversationId}.
     * Each call produces a distinct conversation — functionally a single-turn interaction
     * unless the returned {@code conversationId} is reused in subsequent calls.
     */
    public static ChatRequest of(String message) {
        return new ChatRequest(message, null, null);
    }

    /** Creates a multi-turn request bound to the given {@code conversationId}. */
    public static ChatRequest of(String message, String conversationId) {
        return new ChatRequest(message, conversationId, null);
    }

    /** Creates a full request with conversation memory and application context metadata. */
    public static ChatRequest of(String message, String conversationId, Map<String, Object> context) {
        return new ChatRequest(message, conversationId, context);
    }

    /** Returns {@code true} if application context metadata is present and non-empty. */
    public boolean hasContext() {
        return !context.isEmpty();
    }
}
