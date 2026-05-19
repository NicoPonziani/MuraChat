package com.murachat.core.port.out;

import com.murachat.core.model.ResponseStatus;

/**
 * Secondary port — provides the user-facing message when a query cannot be answered.
 *
 * <p>Called when the domain filter blocks a query ({@link ResponseStatus#OFF_TOPIC})
 * or when no relevant documents are found ({@link ResponseStatus#NO_CONTEXT}).
 *
 * <p>Override this bean to customize the tone and content of refusal messages,
 * e.g., to add app-specific suggestions or localized text.
 */
@FunctionalInterface
public interface FallbackResponseProvider {

    /**
     * Returns the fallback message to show the user.
     *
     * @param query  the original user query that could not be answered
     * @param reason why the query was not answered
     * @return a non-null, user-friendly message
     */
    String getFallbackMessage(String query, ResponseStatus reason);
}
