package com.datastream.application.handler;

import com.datastream.application.command.ActivateStreamCommand;
import com.datastream.application.dto.StreamResponse;
import com.datastream.application.mapper.StreamResponseMapper;
import com.datastream.domain.model.Stream;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.service.StreamDomainService;
import com.datastream.domain.valueobjects.StreamId;

import java.util.Objects;
import java.util.UUID;

/**
 * Handles the {@link ActivateStreamCommand} use case.
 *
 * <p>Loads the stream, transitions it to {@code ACTIVE}, persists the change,
 * and returns the updated {@link StreamResponse}.
 */
public class ActivateStreamHandler {

    private final StreamDomainService streamDomainService;
    private final StreamRepository streamRepository;

    /**
     * Creates the handler with its required dependencies.
     *
     * @param streamDomainService domain service for loading the stream; must not be null
     * @param streamRepository    repository for persisting the activated stream; must not be null
     */
    public ActivateStreamHandler(StreamDomainService streamDomainService, StreamRepository streamRepository) {
        this.streamDomainService = Objects.requireNonNull(streamDomainService, "streamDomainService must not be null");
        this.streamRepository = Objects.requireNonNull(streamRepository, "streamRepository must not be null");
    }

    /**
     * Executes the activate-stream use case.
     *
     * @param command the command identifying the stream to activate; must not be null
     * @return a {@link StreamResponse} reflecting the {@code ACTIVE} status
     * @throws com.datastream.domain.exception.StreamNotFoundException     if no stream with the given ID exists
     * @throws com.datastream.domain.exception.InvalidStreamStateException if the stream cannot be activated from its current status
     */
    public StreamResponse handle(ActivateStreamCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        StreamId streamId = StreamId.of(UUID.fromString(command.streamId()));
        Stream stream = streamDomainService.getStreamOrThrow(streamId);
        stream.activate();

        Stream saved = streamRepository.save(stream);
        return StreamResponseMapper.toResponse(saved);
    }
}
