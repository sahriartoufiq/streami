package com.datastream.domain.service;

import com.datastream.domain.exception.StreamAlreadyExistsException;
import com.datastream.domain.exception.StreamNotFoundException;
import com.datastream.domain.exception.InvalidStreamStateException;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamStatus;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;

import java.util.Objects;

/**
 * Domain service that encapsulates cross-aggregate business rules for streams.
 *
 * <p>Intended to be used by application-layer command/query handlers.
 * This class has no framework dependencies.
 */
public class StreamDomainService {

    private final StreamRepository streamRepository;

    /**
     * Creates a new {@code StreamDomainService}.
     *
     * @param streamRepository the stream repository; must not be null
     */
    public StreamDomainService(StreamRepository streamRepository) {
        this.streamRepository = Objects.requireNonNull(streamRepository, "streamRepository must not be null");
    }

    /**
     * Validates that no stream with the given name already exists.
     *
     * @param streamName the name to check; must not be null
     * @throws StreamAlreadyExistsException if a stream with this name already exists
     */
    public void validateStreamNameUnique(StreamName streamName) {
        Objects.requireNonNull(streamName, "streamName must not be null");
        if (streamRepository.existsByName(streamName)) {
            throw new StreamAlreadyExistsException(streamName);
        }
    }

    /**
     * Validates that the stream identified by the given ID is currently {@code ACTIVE}.
     *
     * @param streamId the ID of the stream to check; must not be null
     * @throws StreamNotFoundException     if no stream with the given ID exists
     * @throws InvalidStreamStateException if the stream exists but is not {@code ACTIVE}
     */
    public void validateStreamIsActive(StreamId streamId) {
        Stream stream = getStreamOrThrow(streamId);
        if (stream.getStatus() != StreamStatus.ACTIVE) {
            throw new InvalidStreamStateException(
                    "Stream " + streamId.value() + " is not ACTIVE (current status: " + stream.getStatus() + ")");
        }
    }

    /**
     * Loads a stream by ID or throws if it does not exist.
     *
     * @param streamId the ID to look up; must not be null
     * @return the found {@link Stream}
     * @throws StreamNotFoundException if no stream with the given ID exists
     */
    public Stream getStreamOrThrow(StreamId streamId) {
        Objects.requireNonNull(streamId, "streamId must not be null");
        return streamRepository.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));
    }
}
