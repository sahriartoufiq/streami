package com.datastream.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing the unique identity of a {@code Stream}.
 *
 * <p>Wraps a {@link UUID} to prevent primitive obsession and enforce
 * non-null invariants at construction time.
 */
public record StreamId(UUID value) {

    /**
     * Compact constructor — validates that the wrapped value is not null.
     *
     * @param value the UUID to wrap; must not be null
     */
    public StreamId {
        Objects.requireNonNull(value, "StreamId value must not be null");
    }

    /**
     * Creates a {@code StreamId} from an existing {@link UUID}.
     *
     * @param value the UUID; must not be null
     * @return a new {@code StreamId}
     */
    public static StreamId of(UUID value) {
        return new StreamId(value);
    }

    /**
     * Generates a new {@code StreamId} backed by a random UUID.
     *
     * @return a new, unique {@code StreamId}
     */
    public static StreamId generate() {
        return new StreamId(UUID.randomUUID());
    }
}
