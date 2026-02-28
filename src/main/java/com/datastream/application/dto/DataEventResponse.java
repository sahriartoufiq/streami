package com.datastream.application.dto;

import java.util.Map;

/**
 * DTO representing a data event returned from the application layer.
 *
 * @param eventId   UUID string of the event
 * @param streamId  UUID string of the owning stream
 * @param payload   raw binary payload bytes
 * @param metadata  arbitrary string key/value metadata
 * @param timestamp ISO-8601 event timestamp
 */
public record DataEventResponse(
        String eventId,
        String streamId,
        byte[] payload,
        Map<String, String> metadata,
        String timestamp) {
}
