package com.murachat.core.port.out;

import java.util.List;

/**
 * Secondary port — supplies the documents that form the application's knowledge base.
 *
 * <p>MuraChat's default implementation reads files from the classpath path configured
 * via {@code murachat.ingestion.path}. Consuming apps can override this to supply
 * documents programmatically — e.g., from a database, a CMS, or a live API.
 *
 * <p>The return type uses {@code Object} for the document to keep this port
 * free of Spring AI dependencies. The autoconfigure module bridges to
 * {@code org.springframework.ai.document.Document} internally.
 */
@FunctionalInterface
public interface KnowledgeBaseProvider {

    /**
     * Returns the list of documents to index in the vector store.
     *
     * <p>Documents are ingested idempotently — identical content (same hash)
     * will not be re-indexed on repeated calls.
     *
     * @return a non-null, possibly empty list of documents
     */
    List<?> getDocuments();
}
