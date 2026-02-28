package com.datastream.application.handler;

import com.datastream.application.command.DeactivateStreamCommand;
import com.datastream.domain.model.Stream;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.service.StreamDomainService;
import com.datastream.domain.valueobjects.StreamId;

import java.util.Objects;
import java.util.UUID;

/**
 * Handles the {@link DeactivateStreamCommand} use case.
 *
 * <p>Loads the stream, transitions it to {@code INACTIVE}, and persists the change.
 */
public class DeactivateStreamHandler {

    private final StreamDomainService streamDomainService;
    private final StreamRepository streamRepository;

    /**
     * Creates the handler with its required dependencies.
     *
     * @param streamDomainService domain service for loading the stream; must not be null
     * @param streamRepository    repository for persisting the deactivated stream; must not be null
     */
    public DeactivateStreamHandler(StreamDomainService streamDomainService, StreamRepository streamRepository) {
        this.streamDomainService = Objects.requireNonNull(streamDomainService, "streamDomainService must not be null");
        this.streamRepository = Objects.requireNonNull(streamRepository, "streamRepository must not be null");
    }

    /**
     * Executes the deactivate-stream use case.
     *
     * @param command the command identifying the stream to deactivate; must not be null
     * @throws com.datastream.domain.exception.StreamNotFoundException     if no stream with the given ID exists
     * @throws com.datastream.domain.exception.InvalidStreamStateException if the stream is not currently {@code ACTIVE}
     */
    public void handle(DeactivateStreamCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        StreamId streamId = StreamId.of(UUID.fromString(command.streamId()));
        Stream stream = streamDomainService.getStreamOrThrow(streamId);
        stream.deactivate();
        streamRepository.save(stream);
    }
}
