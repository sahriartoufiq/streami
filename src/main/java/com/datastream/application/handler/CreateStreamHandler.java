package com.datastream.application.handler;

import com.datastream.application.command.CreateStreamCommand;
import com.datastream.application.dto.StreamResponse;
import com.datastream.application.mapper.StreamResponseMapper;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamType;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.service.StreamDomainService;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.domain.valueobjects.UserId;

import java.util.Objects;
import java.util.UUID;

/**
 * Handles the {@link CreateStreamCommand} use case.
 *
 * <p>Validates name uniqueness, constructs the domain aggregate,
 * persists it, and returns a {@link StreamResponse}.
 */
public class CreateStreamHandler {

    private final StreamDomainService streamDomainService;
    private final StreamRepository streamRepository;

    /**
     * Creates the handler with its required dependencies.
     *
     * @param streamDomainService domain service for cross-aggregate validation; must not be null
     * @param streamRepository    repository for persisting the new stream; must not be null
     */
    public CreateStreamHandler(StreamDomainService streamDomainService, StreamRepository streamRepository) {
        this.streamDomainService = Objects.requireNonNull(streamDomainService, "streamDomainService must not be null");
        this.streamRepository = Objects.requireNonNull(streamRepository, "streamRepository must not be null");
    }

    /**
     * Executes the create-stream use case.
     *
     * @param command the command carrying the creation parameters; must not be null
     * @return a {@link StreamResponse} representing the newly created stream
     * @throws com.datastream.domain.exception.StreamAlreadyExistsException if the name is already taken
     * @throws IllegalArgumentException                                      if {@code streamType} is not a valid enum value
     */
    public StreamResponse handle(CreateStreamCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        StreamName name = StreamName.of(command.name());
        streamDomainService.validateStreamNameUnique(name);

        UserId ownerId = UserId.of(UUID.fromString(command.ownerId()));
        StreamType streamType = StreamType.valueOf(command.streamType());

        Stream stream = Stream.create(name, command.description(), ownerId, streamType);
        Stream saved = streamRepository.save(stream);
        return StreamResponseMapper.toResponse(saved);
    }
}
