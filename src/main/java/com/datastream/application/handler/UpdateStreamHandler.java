package com.datastream.application.handler;

import com.datastream.application.command.UpdateStreamCommand;
import com.datastream.application.dto.StreamResponse;
import com.datastream.application.mapper.StreamResponseMapper;
import com.datastream.domain.model.Stream;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.service.StreamDomainService;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;

import java.util.Objects;
import java.util.UUID;

/**
 * Handles the {@link UpdateStreamCommand} use case.
 *
 * <p>Loads the stream via the domain service, applies the configuration update,
 * persists the change, and returns the updated {@link StreamResponse}.
 */
public class UpdateStreamHandler {

    private final StreamDomainService streamDomainService;
    private final StreamRepository streamRepository;

    /**
     * Creates the handler with its required dependencies.
     *
     * @param streamDomainService domain service for loading the stream; must not be null
     * @param streamRepository    repository for persisting the updated stream; must not be null
     */
    public UpdateStreamHandler(StreamDomainService streamDomainService, StreamRepository streamRepository) {
        this.streamDomainService = Objects.requireNonNull(streamDomainService, "streamDomainService must not be null");
        this.streamRepository = Objects.requireNonNull(streamRepository, "streamRepository must not be null");
    }

    /**
     * Executes the update-stream use case.
     *
     * @param command the command carrying the update parameters; must not be null
     * @return a {@link StreamResponse} reflecting the updated state
     * @throws com.datastream.domain.exception.StreamNotFoundException        if no stream with the given ID exists
     * @throws com.datastream.domain.exception.InvalidStreamOperationException if the stream is deleted
     */
    public StreamResponse handle(UpdateStreamCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        StreamId streamId = StreamId.of(UUID.fromString(command.streamId()));
        Stream stream = streamDomainService.getStreamOrThrow(streamId);
        stream.updateConfig(StreamName.of(command.name()), command.description());

        Stream saved = streamRepository.save(stream);
        return StreamResponseMapper.toResponse(saved);
    }
}
