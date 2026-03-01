package com.datastream.domain.model;

import com.datastream.domain.valueobjects.UserId;

/**
 * Encapsulates optional filter criteria for querying streams.
 *
 * <p>All fields are nullable; a {@code null} value means "no filter on this dimension".
 *
 * @param ownerId    filter by owning user; {@code null} means any owner
 * @param status     filter by lifecycle status; {@code null} means any status
 * @param streamType filter by stream type; {@code null} means any type
 */
public record StreamFilter(
        UserId ownerId,
        StreamStatus status,
        StreamType streamType) {

    /**
     * Returns an empty filter that matches all streams.
     *
     * @return a {@code StreamFilter} with all criteria set to {@code null}
     */
    public static StreamFilter empty() {
        return new StreamFilter(null, null, null);
    }
}
