package com.murachat.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatResponseTest {

    @Test
    void onTopic_isSuccessful() {
        var response = ChatResponse.onTopic("Here is your answer.", "conv-1");

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.status()).isEqualTo(ResponseStatus.ON_TOPIC);
    }

    @Test
    void offTopic_isNotSuccessful() {
        var response = ChatResponse.offTopic("I can only answer app-related questions.", "conv-1");

        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.status()).isEqualTo(ResponseStatus.OFF_TOPIC);
    }

    @Test
    void noContext_isNotSuccessful() {
        var response = ChatResponse.noContext("No relevant documentation found.", "conv-1");

        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.status()).isEqualTo(ResponseStatus.NO_CONTEXT);
    }

    @Test
    void error_hasErrorStatus() {
        var response = ChatResponse.error("Service temporarily unavailable.", "conv-1");

        assertThat(response.status()).isEqualTo(ResponseStatus.ERROR);
        assertThat(response.isSuccessful()).isFalse();
    }
}
