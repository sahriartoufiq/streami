package com.datastream.domain.exception;

/**
 * Base class for all domain-specific exceptions.
 *
 * <p>Carries a machine-readable {@code errorCode} alongside the human-readable
 * message so that gRPC interceptors can map domain failures to precise
 * {@link io.grpc.Status} codes without coupling the domain layer to gRPC.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    /**
     * Creates a domain exception with an error code and descriptive message.
     *
     * @param errorCode machine-readable code (e.g. {@code "STREAM_NOT_FOUND"})
     * @param message   human-readable description of the failure
     */
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the machine-readable error code for this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
