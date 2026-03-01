package com.datastream.infrastructure.persistence.mapper;

import com.datastream.domain.model.DataEvent;
import com.datastream.domain.valueobjects.EventPayload;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.infrastructure.persistence.entity.DataEventJpaEntity;

import java.util.Collections;
import java.util.Map;

/**
 * Bidirectional mapper between the {@link DataEvent} domain entity and
 * {@link DataEventJpaEntity} JPA entity.
 *
 * <p>Uses static methods only; not intended to be instantiated.
 */
public final class DataEventEntityMapper {

    private DataEventEntityMapper() {
    }

    /**
     * Converts a {@link DataEvent} domain entity to a {@link DataEventJpaEntity}.
     *
     * @param event the domain event to convert; must not be null
     * @return a JPA entity ready to be persisted
     */
    public static DataEventJpaEntity toJpaEntity(DataEvent event) {
        return new DataEventJpaEntity(
                event.getEventId(),
                event.getStreamId().value(),
                event.getPayload().value(),
                event.getMetadata().isEmpty() ? null : event.getMetadata(),
                event.getTimestamp()
        );
    }

    /**
     * Reconstitutes a {@link DataEvent} domain entity from a {@link DataEventJpaEntity}.
     *
     * @param entity the JPA entity to convert; must not be null
     * @return the corresponding domain entity
     */
    public static DataEvent toDomain(DataEventJpaEntity entity) {
        Map<String, String> metadata = entity.getMetadata() != null
                ? entity.getMetadata()
                : Collections.emptyMap();

        return DataEvent.reconstitute(
                entity.getEventId(),
                StreamId.of(entity.getStreamId()),
                EventPayload.of(entity.getPayload()),
                metadata,
                entity.getTimestamp()
        );
    }
}
