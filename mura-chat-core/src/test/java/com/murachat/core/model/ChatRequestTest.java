package com.murachat.core.model;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRequestTest {

    @Test
    void ofMessage_createsStatelessRequest() {
        var request = ChatRequest.of("Hello");

        assertThat(request.message()).isEqualTo("Hello");
        assertThat(request.conversationId()).isNull();
        assertThat(request.context()).isNull();
        assertThat(request.hasConversation()).isFalse();
        assertThat(request.hasContext()).isFalse();
    }

    @Test
    void ofMessageAndConversationId_createsMultiTurnRequest() {
        var request = ChatRequest.of("Hello", "conv-123");

        assertThat(request.hasConversation()).isTrue();
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
    void blankConversationId_treatedAsNoConversation() {
        var request = ChatRequest.of("Hello", "   ");

        assertThat(request.hasConversation()).isFalse();
    }
}
