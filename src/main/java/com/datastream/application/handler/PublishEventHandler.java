package com.datastream.application.handler;

import com.datastream.application.command.PublishEventCommand;
import com.datastream.application.dto.DataEventResponse;
import com.datastream.application.mapper.DataEventResponseMapper;
import com.datastream.application.port.StreamEventPublisher;
import com.datastream.domain.model.DataEvent;
import com.datastream.domain.repository.DataEventRepository;
import com.datastream.domain.service.StreamDomainService;
import com.datastream.domain.valueobjects.EventPayload;
import com.datastream.domain.valueobjects.StreamId;

import java.util.Objects;
import java.util.UUID;

/**
 * Handles the {@link PublishEventCommand} use case.
 *
 * <p>Validates the target stream is active, creates and persists a
 * {@link DataEvent}, and optionally notifies registered subscribers
 * via the {@link StreamEventPublisher} port.
 */
public class PublishEventHandler {

    private final StreamDomainService streamDomainService;
    private final DataEventRepository dataEventRepository;
    private final StreamEventPublisher eventPublisher;

    /**
     * Creates the handler with its required dependencies.
     *
     * @param streamDomainService  domain service for validating stream state; must not be null
     * @param dataEventRepository  repository for persisting the event; must not be null
     * @param eventPublisher       optional publisher for notifying subscribers; may be {@code null}
     *                             if no subscriber notification is required
     */
    public PublishEventHandler(
            StreamDomainService streamDomainService,
            DataEventRepository dataEventRepository,
            StreamEventPublisher eventPublisher) {
        this.streamDomainService = Objects.requireNonNull(streamDomainService, "streamDomainService must not be null");
        this.dataEventRepository = Objects.requireNonNull(dataEventRepository, "dataEventRepository must not be null");
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes the publish-event use case.
     *
     * @param command the command carrying the event data; must not be null
     * @return a {@link DataEventResponse} representing the persisted event
     * @throws com.datastream.domain.exception.StreamNotFoundException     if no stream with the given ID exists
     * @throws com.datastream.domain.exception.InvalidStreamStateException if the stream is not {@code ACTIVE}
     */
    public DataEventResponse handle(PublishEventCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        StreamId streamId = StreamId.of(UUID.fromString(command.streamId()));
        streamDomainService.validateStreamIsActive(streamId);

        EventPayload payload = EventPayload.of(command.payload());
        DataEvent event = DataEvent.create(streamId, payload, command.metadata());
        DataEvent saved = dataEventRepository.save(event);

        DataEventResponse response = DataEventResponseMapper.toResponse(saved);

        if (eventPublisher != null) {
            eventPublisher.publish(response);
        }

        return response;
    }
}
