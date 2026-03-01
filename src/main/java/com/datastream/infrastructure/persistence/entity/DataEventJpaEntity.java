package com.datastream.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * JPA entity mapping the {@code data_events} database table.
 *
 * <p>The {@code metadata} column is stored as PostgreSQL {@code JSONB} using
 * Hibernate 6's {@link JdbcTypeCode} with {@link SqlTypes#JSON}.
 * The {@code payload} column uses PostgreSQL {@code BYTEA} for efficient binary storage.
 */
@Entity
@Table(name = "data_events")
public class DataEventJpaEntity {

    @Id
    @Column(name = "event_id", updatable = false, nullable = false)
    private UUID eventId;

    @Column(name = "stream_id", nullable = false)
    private UUID streamId;

    /**
     * Binary payload stored as PostgreSQL {@code BYTEA}.
     * Using {@code columnDefinition = "BYTEA"} instead of {@code @Lob} for
     * correct behaviour with Hibernate 6 + PostgreSQL (avoids OID/LOB API).
     */
    @Column(name = "payload", nullable = false, columnDefinition = "BYTEA")
    private byte[] payload;

    /** Arbitrary string key/value pairs stored as PostgreSQL {@code JSONB}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, String> metadata;

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant timestamp;

    /** No-arg constructor required by JPA. */
    protected DataEventJpaEntity() {
    }

    /**
     * All-args constructor for creating a fully populated entity.
     *
     * @param eventId   the event UUID (primary key)
     * @param streamId  UUID of the owning stream
     * @param payload   raw binary payload
     * @param metadata  arbitrary key/value metadata; may be null
     * @param timestamp event creation timestamp
     */
    public DataEventJpaEntity(UUID eventId, UUID streamId, byte[] payload,
                               Map<String, String> metadata, Instant timestamp) {
        this.eventId = eventId;
        this.streamId = streamId;
        this.payload = payload;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }

    /** @return the event UUID (primary key) */
    public UUID getEventId() { return eventId; }

    /** @return UUID of the stream this event belongs to */
    public UUID getStreamId() { return streamId; }

    /** @return the raw binary payload */
    public byte[] getPayload() { return payload; }

    /** @return the metadata map, may be null */
    public Map<String, String> getMetadata() { return metadata; }

    /** @return the event timestamp */
    public Instant getTimestamp() { return timestamp; }
}
