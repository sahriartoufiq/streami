package com.datastream.application.mapper;

import com.datastream.application.dto.DataEventResponse;
import com.datastream.domain.model.DataEvent;

/**
 * Utility class that maps {@link DataEvent} domain entities to {@link DataEventResponse} DTOs.
 *
 * <p>Uses static methods only; not intended to be instantiated.
 */
public final class DataEventResponseMapper {

    private DataEventResponseMapper() {
    }

    /**
     * Maps a {@link DataEvent} entity to a {@link DataEventResponse} DTO.
     *
     * @param event the domain event to map; must not be null
     * @return a {@link DataEventResponse} with all scalar fields serialised to strings
     */
    public static DataEventResponse toResponse(DataEvent event) {
        return new DataEventResponse(
                event.getEventId().toString(),
                event.getStreamId().value().toString(),
                event.getPayload().value(),
                event.getMetadata(),
                event.getTimestamp().toString()
        );
    }
}
