package com.datastream.domain.exception;

import com.datastream.domain.valueobjects.StreamName;

/**
 * Thrown when attempting to create a stream with a name that already exists.
 */
public class StreamAlreadyExistsException extends DomainException {

    private static final String ERROR_CODE = "STREAM_ALREADY_EXISTS";

    /**
     * Creates the exception for a duplicate stream name.
     *
     * @param streamName the name that already exists
     */
    public StreamAlreadyExistsException(StreamName streamName) {
        super(ERROR_CODE, "Stream already exists with name: " + streamName.value());
    }
}
