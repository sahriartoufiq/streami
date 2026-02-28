package com.datastream.application.query;

/**
 * Query to retrieve a paginated, optionally filtered list of streams.
 *
 * @param ownerId    UUID string to filter by owner; {@code null} means any owner
 * @param status     {@link com.datastream.domain.model.StreamStatus} name to filter by; {@code null} means any
 * @param streamType {@link com.datastream.domain.model.StreamType} name to filter by; {@code null} means any
 * @param page       zero-based page index; must be >= 0
 * @param size       maximum results per page; must be > 0
 */
public record ListStreamsQuery(
        String ownerId,
        String status,
        String streamType,
        int page,
        int size) {
}
