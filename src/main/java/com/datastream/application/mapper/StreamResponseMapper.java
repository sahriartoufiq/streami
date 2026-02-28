package com.datastream.application.mapper;

import com.datastream.application.dto.StreamResponse;
import com.datastream.domain.model.Stream;

/**
 * Utility class that maps {@link Stream} domain aggregate roots to {@link StreamResponse} DTOs.
 *
 * <p>Uses static methods only; not intended to be instantiated.
 */
public final class StreamResponseMapper {

    private StreamResponseMapper() {
    }

    /**
     * Maps a {@link Stream} aggregate root to a {@link StreamResponse} DTO.
     *
     * @param stream the domain stream to map; must not be null
     * @return a {@link StreamResponse} with all fields serialised to strings
     */
    public static StreamResponse toResponse(Stream stream) {
        return new StreamResponse(
                stream.getId().value().toString(),
                stream.getName().value(),
                stream.getDescription(),
                stream.getOwnerId().value().toString(),
                stream.getStreamType().name(),
                stream.getStatus().name(),
                stream.getCreatedAt().toString(),
                stream.getUpdatedAt().toString()
        );
    }
}
