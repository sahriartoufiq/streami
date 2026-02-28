package com.datastream.domain.exception;

/**
 * Thrown when an operation is attempted on a stream that does not permit it
 * (e.g. updating a deleted stream).
 */
public class InvalidStreamOperationException extends DomainException {

    private static final String ERROR_CODE = "INVALID_STREAM_OPERATION";

    /**
     * Creates the exception with a descriptive message.
     *
     * @param message explanation of the illegal operation
     */
    public InvalidStreamOperationException(String message) {
        super(ERROR_CODE, message);
    }
}
