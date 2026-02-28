package com.datastream.domain.exception;

import com.datastream.domain.valueobjects.StreamId;

/**
 * Thrown when a requested stream cannot be found in the repository.
 */
public class StreamNotFoundException extends DomainException {

    private static final String ERROR_CODE = "STREAM_NOT_FOUND";

    /**
     * Creates the exception for a missing stream by ID.
     *
     * @param streamId the ID of the stream that was not found
     */
    public StreamNotFoundException(StreamId streamId) {
        super(ERROR_CODE, "Stream not found: " + streamId.value());
    }

    /**
     * Creates the exception with a custom message.
     *
     * @param message descriptive message
     */
    public StreamNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
