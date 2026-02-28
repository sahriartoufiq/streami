package com.datastream.domain.exception;

/**
 * Thrown when a stream state transition is not permitted from the current status.
 */
public class InvalidStreamStateException extends DomainException {

    private static final String ERROR_CODE = "INVALID_STREAM_STATE";

    /**
     * Creates the exception with a descriptive message.
     *
     * @param message explanation of which transition was illegal
     */
    public InvalidStreamStateException(String message) {
        super(ERROR_CODE, message);
    }
}
