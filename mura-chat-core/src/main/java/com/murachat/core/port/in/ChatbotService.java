package com.murachat.core.port.in;

import com.murachat.core.model.ChatRequest;
import com.murachat.core.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * Primary port — the single public contract between MuraChat and any consuming application.
 *
 * <p>This interface lives in the {@code core} module and has zero Spring dependencies.
 * The consuming app interacts exclusively through this interface; it never depends
 * on {@code autoconfigure} internals.
 *
 * <p><strong>HTTP exposure is the consuming app's responsibility.</strong>
 * MuraChat does not register any REST controllers. The app creates its own
 * controller, calls this service, and applies its own security policy.
 */
public interface ChatbotService {

    /**
     * Processes a user message and returns a complete response.
     *
     * <p>The response {@link com.murachat.core.model.ResponseStatus} communicates
     * the semantic outcome:
     * <ul>
     *   <li>{@code ON_TOPIC} — answer generated from knowledge base context</li>
     *   <li>{@code OFF_TOPIC} — query blocked before reaching the LLM</li>
     *   <li>{@code NO_CONTEXT} — in-topic query but no relevant documents found</li>
     *   <li>{@code ERROR} — infrastructure failure</li>
     * </ul>
     *
     * @param request the user's chat request
     * @return a fully populated {@link ChatResponse}; never null
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Streams the response token-by-token for real-time UI rendering.
     *
     * <p>Default implementation wraps the synchronous {@link #chat(ChatRequest)} result
     * into a single-element {@link Flux}. Override in the implementing class to enable
     * true token streaming via Spring AI's streaming API.
     *
     * @param request the user's chat request
     * @return a {@link Flux} emitting response tokens as they are generated
     */
    default Flux<String> stream(ChatRequest request) {
        return Flux.just(chat(request).content());
    }
}
