package com.datastream.application.dto;

/**
 * DTO representing a stream returned from the application layer.
 *
 * <p>All values are serialised to {@link String} so this record is free of
 * any domain or framework types and safe to hand directly to the gRPC adapter.
 *
 * @param id          UUID string of the stream
 * @param name        stream name
 * @param description optional description; may be null
 * @param ownerId     UUID string of the owning user
 * @param streamType  name of the {@link com.datastream.domain.model.StreamType} enum value
 * @param status      name of the {@link com.datastream.domain.model.StreamStatus} enum value
 * @param createdAt   ISO-8601 creation timestamp
 * @param updatedAt   ISO-8601 last-updated timestamp
 */
public record StreamResponse(
        String id,
        String name,
        String description,
        String ownerId,
        String streamType,
        String status,
        String createdAt,
        String updatedAt) {
}
