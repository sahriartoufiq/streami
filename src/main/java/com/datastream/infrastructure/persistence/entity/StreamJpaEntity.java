package com.datastream.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping the {@code streams} database table.
 *
 * <p>This class is an infrastructure concern only. Domain logic must never
 * depend on it directly — use the mapper to convert to/from the domain
 * {@link com.datastream.domain.model.Stream} aggregate.
 */
@Entity
@Table(name = "streams")
public class StreamJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "stream_type", nullable = false, length = 20)
    private String streamType;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant updatedAt;

    /** No-arg constructor required by JPA. */
    protected StreamJpaEntity() {
    }

    /**
     * All-args constructor for creating a fully populated entity.
     *
     * @param id          the stream UUID
     * @param name        the stream name
     * @param description optional description
     * @param ownerId     UUID of the owning user
     * @param streamType  string representation of {@link com.datastream.domain.model.StreamType}
     * @param status      string representation of {@link com.datastream.domain.model.StreamStatus}
     * @param createdAt   creation timestamp
     * @param updatedAt   last-updated timestamp
     */
    public StreamJpaEntity(UUID id, String name, String description, UUID ownerId,
                           String streamType, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.streamType = streamType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** @return the stream UUID (primary key) */
    public UUID getId() { return id; }

    /** @return the stream name */
    public String getName() { return name; }

    /** @return optional description, may be null */
    public String getDescription() { return description; }

    /** @return UUID of the owning user */
    public UUID getOwnerId() { return ownerId; }

    /** @return stream type string (e.g. "EVENT") */
    public String getStreamType() { return streamType; }

    /** @return stream status string (e.g. "ACTIVE") */
    public String getStatus() { return status; }

    /** @return creation timestamp */
    public Instant getCreatedAt() { return createdAt; }

    /** @return last-updated timestamp */
    public Instant getUpdatedAt() { return updatedAt; }

    /** @param status the new status to persist */
    public void setStatus(String status) { this.status = status; }

    /** @param name the new name to persist */
    public void setName(String name) { this.name = name; }

    /** @param description the new description to persist */
    public void setDescription(String description) { this.description = description; }

    /** @param updatedAt the new last-updated timestamp */
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
