package com.datastream.infrastructure.persistence.mapper;

import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamStatus;
import com.datastream.domain.model.StreamType;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.domain.valueobjects.UserId;
import com.datastream.infrastructure.persistence.entity.StreamJpaEntity;

/**
 * Bidirectional mapper between the {@link Stream} domain aggregate and
 * {@link StreamJpaEntity} JPA entity.
 *
 * <p>Uses static methods only; not intended to be instantiated.
 */
public final class StreamEntityMapper {

    private StreamEntityMapper() {
    }

    /**
     * Converts a {@link Stream} domain aggregate to a {@link StreamJpaEntity}.
     *
     * @param stream the domain object to convert; must not be null
     * @return a JPA entity ready to be persisted
     */
    public static StreamJpaEntity toJpaEntity(Stream stream) {
        return new StreamJpaEntity(
                stream.getId().value(),
                stream.getName().value(),
                stream.getDescription(),
                stream.getOwnerId().value(),
                stream.getStreamType().name(),
                stream.getStatus().name(),
                stream.getCreatedAt(),
                stream.getUpdatedAt()
        );
    }

    /**
     * Reconstitutes a {@link Stream} domain aggregate from a {@link StreamJpaEntity}.
     *
     * @param entity the JPA entity to convert; must not be null
     * @return the corresponding domain aggregate
     */
    public static Stream toDomain(StreamJpaEntity entity) {
        return Stream.reconstitute(
                StreamId.of(entity.getId()),
                StreamName.of(entity.getName()),
                entity.getDescription(),
                UserId.of(entity.getOwnerId()),
                StreamType.valueOf(entity.getStreamType()),
                StreamStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
