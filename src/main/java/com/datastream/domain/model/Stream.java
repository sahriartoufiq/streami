package com.datastream.domain.model;

import com.datastream.domain.exception.InvalidStreamOperationException;
import com.datastream.domain.exception.InvalidStreamStateException;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.domain.valueobjects.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root representing a configured data stream.
 *
 * <p>All state mutations are performed through named business methods that
 * enforce domain invariants. The constructor is private; use the static
 * factory {@link #create} for new streams or {@link #reconstitute} when
 * rehydrating from persistence.
 */
public final class Stream {

    private final StreamId id;
    private StreamName name;
    private String description;
    private final UserId ownerId;
    private StreamType streamType;
    private StreamStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Stream(
            StreamId id,
            StreamName name,
            String description,
            UserId ownerId,
            StreamType streamType,
            StreamStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
        this.streamType = Objects.requireNonNull(streamType, "streamType must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * Creates a new stream in {@link StreamStatus#DRAFT} status with timestamps set to now.
     *
     * @param name        the stream name; must not be null
     * @param description optional human-readable description; may be null
     * @param ownerId     the owning user; must not be null
     * @param streamType  the kind of data flowing through this stream; must not be null
     * @return a fully initialised {@code Stream} aggregate root
     */
    public static Stream create(StreamName name, String description, UserId ownerId, StreamType streamType) {
        Instant now = Instant.now();
        return new Stream(StreamId.generate(), name, description, ownerId, streamType, StreamStatus.DRAFT, now, now);
    }

    /**
     * Reconstitutes a {@code Stream} from persisted state (e.g. a JPA entity).
     *
     * @param id          the persisted stream ID
     * @param name        the persisted name
     * @param description the persisted description
     * @param ownerId     the persisted owner ID
     * @param streamType  the persisted stream type
     * @param status      the persisted status
     * @param createdAt   the original creation timestamp
     * @param updatedAt   the last-updated timestamp
     * @return a {@code Stream} reflecting the persisted state
     */
    public static Stream reconstitute(
            StreamId id,
            StreamName name,
            String description,
            UserId ownerId,
            StreamType streamType,
            StreamStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new Stream(id, name, description, ownerId, streamType, status, createdAt, updatedAt);
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Transitions the stream to {@link StreamStatus#ACTIVE}.
     *
     * @throws InvalidStreamStateException if the current status is not
     *         {@code DRAFT} or {@code INACTIVE}
     */
    public void activate() {
        if (status != StreamStatus.DRAFT && status != StreamStatus.INACTIVE) {
            throw new InvalidStreamStateException(
                    "Cannot activate stream from status " + status + ". Allowed from: DRAFT, INACTIVE");
        }
        this.status = StreamStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions the stream to {@link StreamStatus#INACTIVE}.
     *
     * @throws InvalidStreamStateException if the current status is not {@code ACTIVE}
     */
    public void deactivate() {
        if (status != StreamStatus.ACTIVE) {
            throw new InvalidStreamStateException(
                    "Cannot deactivate stream from status " + status + ". Allowed from: ACTIVE");
        }
        this.status = StreamStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Soft-deletes the stream by transitioning it to {@link StreamStatus#DELETED}.
     *
     * @throws InvalidStreamStateException if the stream is already deleted
     */
    public void softDelete() {
        if (status == StreamStatus.DELETED) {
            throw new InvalidStreamStateException("Stream is already in DELETED status");
        }
        this.status = StreamStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the name and description of the stream.
     *
     * @param name        the new name; must not be null
     * @param description the new description; may be null
     * @throws InvalidStreamOperationException if the stream is in {@code DELETED} status
     */
    public void updateConfig(StreamName name, String description) {
        if (status == StreamStatus.DELETED) {
            throw new InvalidStreamOperationException("Cannot update configuration of a DELETED stream");
        }
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.updatedAt = Instant.now();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** @return the unique stream identifier */
    public StreamId getId() { return id; }

    /** @return the stream name */
    public StreamName getName() { return name; }

    /** @return the optional description, may be null */
    public String getDescription() { return description; }

    /** @return the ID of the user who owns this stream */
    public UserId getOwnerId() { return ownerId; }

    /** @return the stream type */
    public StreamType getStreamType() { return streamType; }

    /** @return the current lifecycle status */
    public StreamStatus getStatus() { return status; }

    /** @return the timestamp when this stream was created */
    public Instant getCreatedAt() { return createdAt; }

    /** @return the timestamp of the most recent state change */
    public Instant getUpdatedAt() { return updatedAt; }
}
