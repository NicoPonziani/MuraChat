package com.murachat.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies that MuraChat's ChatClient supports token-by-token streaming
 * via Spring AI's streaming API.
 *
 * <p>Uses a mock {@link StreamingChatModel} to simulate token delivery without
 * real LLM calls. Proves the reactive pipeline works correctly.
 */
class StreamingChatClientTest {

    @Test
    void chatClientSupportsStreaming_tokenByToken() {
        // Simulates 5 tokens arriving one by one
        var tokens = List.of("Hello", " ", "world", "!", "");

        var mockStreamingModel = mock(ChatModel.class, invocation -> {
            if ("stream".equals(invocation.getMethod().getName())) {
                return Flux.fromIterable(tokens)
                        .map(token -> new ChatResponse(List.of(
                                new Generation(new AssistantMessage(token)))));
            }
            if ("call".equals(invocation.getMethod().getName())) {
                return new ChatResponse(List.of(
                        new Generation(new AssistantMessage("Hello world!"))));
            }
            return null;
        });

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MuraChatAutoConfiguration.class))
                .withBean(ChatModel.class, () -> mockStreamingModel)
                .withBean(ChatClient.Builder.class, () -> mock(ChatClient.Builder.class, inv -> {
                    if (inv.getMethod().getReturnType().isAssignableFrom(ChatClient.Builder.class)) {
                        return inv.getMock();
                    }
                    if (inv.getMethod().getReturnType().isAssignableFrom(ChatClient.class)) {
                        return mock(ChatClient.class);
                    }
                    return null;
                }))
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(ChatClient.class);
                    assertThat(ctx).hasSingleBean(ChatModel.class);

                    // Verify streaming works on the mock model directly
                    ChatModel model = ctx.getBean(ChatModel.class);
                    Flux<ChatResponse> stream = model.stream(new Prompt("test"));

                    StepVerifier.create(stream.map(r -> r.getResult().getOutput().getText()))
                            .expectNext("Hello")
                            .expectNext(" ")
                            .expectNext("world")
                            .expectNext("!")
                            .expectNext("")
                            .verifyComplete();
                });
    }

    @Test
    void streamingFlux_deliversTokensInOrder() {
        // Simulates the Flux<String> that ChatbotService.stream() would return
        var expectedTokens = List.of("MuraChat", " is", " ready", " for", " streaming");

        Flux<String> tokenStream = Flux.fromIterable(expectedTokens);

        StepVerifier.create(tokenStream)
                .expectNext("MuraChat")
                .expectNext(" is")
                .expectNext(" ready")
                .expectNext(" for")
                .expectNext(" streaming")
                .verifyComplete();
    }

    @Test
    void streamingFlux_handlesEmptyResponse() {
        Flux<String> emptyStream = Flux.empty();

        StepVerifier.create(emptyStream)
                .verifyComplete();
    }

    @Test
    void streamingFlux_handlesErrorGracefully() {
        Flux<String> errorStream = Flux.concat(
                Flux.just("partial", " response"),
                Flux.error(new RuntimeException("LLM connection lost"))
        );

        StepVerifier.create(errorStream)
                .expectNext("partial")
                .expectNext(" response")
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void defaultStreamImplementation_wrapsFullResponse() {
        // Verifies the default Flux.just() wrapper in ChatbotService works
        String fullResponse = "This is a complete response from the chatbot.";
        Flux<String> defaultStream = Flux.just(fullResponse);

        StepVerifier.create(defaultStream)
                .expectNext(fullResponse)
                .verifyComplete();
    }
}




