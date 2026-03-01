package com.datastream.domain.model;

import com.datastream.domain.valueobjects.EventPayload;
import com.datastream.domain.valueobjects.StreamId;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a single data event published to or received from a stream.
 *
 * <p>Instances are created via the static factory {@link #create}. The constructor
 * is private to enforce invariants.
 */
public final class DataEvent {

    private final UUID eventId;
    private final StreamId streamId;
    private final EventPayload payload;
    private final Map<String, String> metadata;
    private final Instant timestamp;

    private DataEvent(
            UUID eventId,
            StreamId streamId,
            EventPayload payload,
            Map<String, String> metadata,
            Instant timestamp) {
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.streamId = Objects.requireNonNull(streamId, "streamId must not be null");
        this.payload = Objects.requireNonNull(payload, "payload must not be null");
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /**
     * Creates a new {@code DataEvent} with a generated ID and the current timestamp.
     *
     * @param streamId the stream this event belongs to; must not be null
     * @param payload  the binary payload; must not be null
     * @param metadata arbitrary string key/value metadata; must not be null, use empty map if none
     * @return a new {@code DataEvent}
     */
    public static DataEvent create(StreamId streamId, EventPayload payload, Map<String, String> metadata) {
        return new DataEvent(UUID.randomUUID(), streamId, payload, metadata, Instant.now());
    }

    /**
     * Reconstitutes a {@code DataEvent} from persisted state.
     *
     * @param eventId   the persisted event ID
     * @param streamId  the owning stream ID
     * @param payload   the persisted payload
     * @param metadata  the persisted metadata
     * @param timestamp the persisted timestamp
     * @return a {@code DataEvent} reflecting the persisted state
     */
    public static DataEvent reconstitute(
            UUID eventId,
            StreamId streamId,
            EventPayload payload,
            Map<String, String> metadata,
            Instant timestamp) {
        return new DataEvent(eventId, streamId, payload, metadata, timestamp);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** @return the unique event identifier */
    public UUID getEventId() { return eventId; }

    /** @return the ID of the stream this event belongs to */
    public StreamId getStreamId() { return streamId; }

    /** @return the binary payload of this event */
    public EventPayload getPayload() { return payload; }

    /** @return an unmodifiable view of the event metadata */
    public Map<String, String> getMetadata() { return metadata; }

    /** @return the timestamp when this event was created */
    public Instant getTimestamp() { return timestamp; }
}
