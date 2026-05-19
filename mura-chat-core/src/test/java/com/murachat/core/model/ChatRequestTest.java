package com.murachat.core.model;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRequestTest {

    @Test
    void ofMessage_autoGeneratesConversationId() {
        var request = ChatRequest.of("Hello");

        assertThat(request.message()).isEqualTo("Hello");
        // conversationId is always guaranteed — auto-generated via UUID if not provided
        assertThat(request.conversationId()).isNotBlank();
        // context defaults to Map.of(), never null
        assertThat(request.context()).isEmpty();
        assertThat(request.hasContext()).isFalse();
    }

    @Test
    void ofMessage_eachCallGeneratesDistinctConversationId() {
        var r1 = ChatRequest.of("Hello");
        var r2 = ChatRequest.of("Hello");

        assertThat(r1.conversationId()).isNotEqualTo(r2.conversationId());
    }

    @Test
    void ofMessageAndConversationId_preservesProvidedId() {
        var request = ChatRequest.of("Hello", "conv-123");

        assertThat(request.conversationId()).isEqualTo("conv-123");
    }

    @Test
    void ofFullRequest_carriesContextMetadata() {
        var ctx = Map.<String, Object>of("page", "/settings");
        var request = ChatRequest.of("How do I change my password?", "conv-456", ctx);

        assertThat(request.hasContext()).isTrue();
        assertThat(request.context()).containsKey("page");
    }

    @Test
    void nullContext_normalisedToEmptyMap() {
        // compact constructor normalises null context → Map.of()
        var request = new ChatRequest("Hello", "conv-789", null);

        assertThat(request.context()).isEmpty();
        assertThat(request.hasContext()).isFalse();
    }
}
