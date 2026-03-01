package com.datastream.infrastructure.interceptor;

import com.datastream.domain.exception.InvalidStreamOperationException;
import com.datastream.domain.exception.InvalidStreamStateException;
import com.datastream.domain.exception.StreamAlreadyExistsException;
import com.datastream.domain.exception.StreamNotFoundException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

/**
 * Global gRPC exception handler that maps domain exceptions to appropriate
 * {@link io.grpc.Status} codes.
 *
 * <p>Annotated with {@link GrpcAdvice} so that the
 * {@code grpc-spring-boot-starter} registers it for all gRPC service calls.
 * This keeps exception-to-status mapping out of individual service methods.
 */
@GrpcAdvice
public class GrpcExceptionInterceptor {

    /**
     * Maps {@link StreamNotFoundException} to {@link Status#NOT_FOUND}.
     *
     * @param e the exception thrown by the application layer
     * @return a {@link StatusRuntimeException} with {@code NOT_FOUND} status
     */
    @GrpcExceptionHandler(StreamNotFoundException.class)
    public StatusRuntimeException handleStreamNotFound(StreamNotFoundException e) {
        return Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asRuntimeException();
    }

    /**
     * Maps {@link StreamAlreadyExistsException} to {@link Status#ALREADY_EXISTS}.
     *
     * @param e the exception thrown by the application layer
     * @return a {@link StatusRuntimeException} with {@code ALREADY_EXISTS} status
     */
    @GrpcExceptionHandler(StreamAlreadyExistsException.class)
    public StatusRuntimeException handleStreamAlreadyExists(StreamAlreadyExistsException e) {
        return Status.ALREADY_EXISTS
                .withDescription(e.getMessage())
                .asRuntimeException();
    }

    /**
     * Maps {@link InvalidStreamStateException} to {@link Status#FAILED_PRECONDITION}.
     *
     * @param e the exception thrown by the domain layer
     * @return a {@link StatusRuntimeException} with {@code FAILED_PRECONDITION} status
     */
    @GrpcExceptionHandler(InvalidStreamStateException.class)
    public StatusRuntimeException handleInvalidStreamState(InvalidStreamStateException e) {
        return Status.FAILED_PRECONDITION
                .withDescription(e.getMessage())
                .asRuntimeException();
    }

    /**
     * Maps {@link InvalidStreamOperationException} to {@link Status#FAILED_PRECONDITION}.
     *
     * @param e the exception thrown by the domain layer
     * @return a {@link StatusRuntimeException} with {@code FAILED_PRECONDITION} status
     */
    @GrpcExceptionHandler(InvalidStreamOperationException.class)
    public StatusRuntimeException handleInvalidStreamOperation(InvalidStreamOperationException e) {
        return Status.FAILED_PRECONDITION
                .withDescription(e.getMessage())
                .asRuntimeException();
    }

    /**
     * Maps {@link IllegalArgumentException} to {@link Status#INVALID_ARGUMENT}.
     *
     * <p>Covers invalid enum values (e.g. unknown stream type) and malformed UUIDs
     * from the gRPC request.
     *
     * @param e the exception thrown during argument parsing
     * @return a {@link StatusRuntimeException} with {@code INVALID_ARGUMENT} status
     */
    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleIllegalArgument(IllegalArgumentException e) {
        return Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asRuntimeException();
    }
}
