package com.murachat.autoconfigure;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Type-safe configuration properties for MuraChat.
 *
 * <p>All properties are prefixed with {@code murachat}. The
 * {@code spring-boot-configuration-processor} annotation processor generates
 * IDE metadata for autocomplete in {@code application.yml} / {@code application.properties}.
 *
 * <p>Minimal working configuration:
 * <pre>{@code
 * murachat:
 *   llm:
 *     api-key: ${OPENAI_API_KEY}
 *   ingestion:
 *     path: classpath:docs/
 * }</pre>
 */
@Validated
@ConfigurationProperties(prefix = "murachat")
public record ChatbotProperties (
        Boolean enabled,
        @Valid LlmProperties llm,
        @Valid VectorStoreProperties vectorStore,
        @Valid RagProperties rag,
        @Valid MemoryProperties memory,
        @Valid DomainFilterProperties domainFilter,
        @Valid IngestionProperties ingestion,
        @Valid ObservabilityProperties observability
){

    public ChatbotProperties {
        if(enabled == null) enabled = true;
        if(llm == null) llm = new LlmProperties(null, null, null);
        if(vectorStore == null) vectorStore = new VectorStoreProperties(null, null, null);
        if(rag == null) rag = new RagProperties(false, null);
        if(memory == null) memory = new MemoryProperties(null, null, null);
        if(domainFilter == null) domainFilter = new DomainFilterProperties(null, null, null);
        if(ingestion == null) ingestion = new IngestionProperties(null, null, null, null);
        if(observability == null) observability = new ObservabilityProperties(null, null);
    }

    // -------------------------------------------------------------------------
    // Nested configuration classes
    // -------------------------------------------------------------------------

    /**
     * Properties for the underlying LLM. Defaults to OpenAI's gpt-4o model.
     * The API key is intentionally not validated here — Spring AI validates it
     * lazily when the actual {@code ChatClient} is constructed (Sprint 2).
     * This allows tests and Ollama-based setups to run without an API key.
     *
     * @param provider LLM provider name. Supported values: openai, ollama. Default: openai.
     * @param model    LLM model name. Default: gpt-4o for OpenAI.
     * @param apiKey   API key for the LLM provider. Optional here — required by Spring AI at runtime.
     */
    public record LlmProperties(
        String provider,
        String model,
        String apiKey
    ) {
        public LlmProperties{
            if(provider == null || provider.isBlank()) provider = "openai";
            if(model == null || model.isBlank()) model = "gpt-4o";
        }
    }

    /**
     * Properties for the vector store used in RAG. Defaults to pgvector with a 0.75 similarity threshold and top 5 results.
     * @param type Vector store type. Supported values: pgvector, redis, mongodb. Default: pgvector.
     * @param similarityThreshold Minimum similarity score to consider a document relevant. Range: 0.0–1.0.
     * @param topK Number of top documents to retrieve per query.
     */
    public record VectorStoreProperties(
            String type,
            Double similarityThreshold,
            Integer topK
    ) {
        public VectorStoreProperties {
            if(type == null || type.isBlank()) type = "mongodb";
            if(similarityThreshold == null) similarityThreshold = 0.75;
            if(topK == null) topK = 5;
        }
    }

    /**
     * Properties for the RAG (Retrieval-Augmented Generation) component. Defaults to disallowing empty context and using a default system prompt template.
     * @param allowEmptyContext When false (default), the chatbot refuses to answer if no relevant documents are found. When true, the LLM may use its general knowledge. Keep false in production to prevent hallucinations.
     * @param systemPromptTemplate Classpath or filesystem path to the system prompt template used for RAG. Default: classpath:murachat/default-system-prompt.txt
     */
    public record RagProperties(
            boolean allowEmptyContext,
            String systemPromptTemplate
    ) {
        public RagProperties {
            if(systemPromptTemplate == null || systemPromptTemplate.isBlank())
                systemPromptTemplate = "classpath:murachat/default-system-prompt.txt";
        }
    }

    /**
     * Properties for conversation memory management. Defaults to enabled with a sliding window of the last 20 messages or 4000 tokens.
     * @param enabled When true (default), conversation history is retained in memory and included in prompts. When false, only the current user message is sent to the LLM.
     * @param maxMessages Maximum number of messages to keep in the conversation sliding window. Default: 20.
     * @param maxTokens Maximum total tokens in the conversation history before truncation. Default: 4000.
     */
    public record MemoryProperties(
        Boolean enabled,
        Integer maxMessages,
        Integer maxTokens
    ) {
        public MemoryProperties {
            if(enabled == null) enabled = true;
            if(maxMessages == null) maxMessages = 20;
            if(maxTokens == null) maxTokens = 4000;
        }
    }

    /**
     * Properties for domain-specific filtering of retrieved documents. Defaults to enabled with keyword-based filtering and an empty keyword list (passthrough).
     * @param enabled When true (default), retrieved documents are filtered based on the specified mode and keywords. When false, all retrieved documents are included in the RAG context.
     * @param mode Filtering mode. Supported values: KEYWORD (default) for simple keyword matching, LLM for using the LLM to determine relevance based on keywords.
     * @param keywords Explicit list of in-topic keywords. Leave empty for passthrough (no filtering).
     */
    public record DomainFilterProperties(
        Boolean enabled,
        FilterMode mode,
        List<String> keywords
    ) {
        public DomainFilterProperties {
            if(enabled == null) enabled = true;
            if(mode == null) mode = FilterMode.KEYWORD;
            if(keywords == null) keywords = new ArrayList<>();
        }

        public enum FilterMode { KEYWORD, LLM }
    }

    /**
     * Properties for document ingestion. Defaults to ingesting from classpath:knowledge/ on startup, with a chunk size of 512 tokens and an overlap of 64 tokens.
     * @param path Classpath or filesystem path to the knowledge base documents. Default: classpath:knowledge/
     * @param chunkSize Token size for document chunking. Default: 512.
     * @param chunkOverlap Token overlap between consecutive chunks. Default: 64.
     * @param onStartup When true (default), documents are ingested on application startup. When false, ingestion must be triggered manually via an API endpoint or scheduled task.
     */
    public record IngestionProperties(
         String path,
         Integer chunkSize,
         Integer chunkOverlap,
         Boolean onStartup) {
            public IngestionProperties {
                if(path == null || path.isBlank()) path = "classpath:knowledge/";
                if(chunkSize == null) chunkSize = 512;
                if(chunkOverlap == null) chunkOverlap = 64;
                if(onStartup == null) onStartup = true;
            }
    }

    /**
     * Properties for observability and logging. Defaults to not logging prompts or responses for security and privacy reasons.
     * @param logPrompts When true, full prompts are logged at DEBUG level. Enable only in development — prompts may contain sensitive user data.
     * @param logResponses When true, full LLM responses are logged at DEBUG level. Enable only in development — responses may contain sensitive user data.
     */
    public record ObservabilityProperties(
            Boolean logPrompts,
            Boolean logResponses
    ) {
        public ObservabilityProperties {
            if(logPrompts == null) logPrompts = false;
            if(logResponses == null) logResponses = false;
        }
    }
}
