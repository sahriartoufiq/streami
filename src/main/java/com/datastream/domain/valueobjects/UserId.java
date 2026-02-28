package com.datastream.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing the identity of a user who owns a {@code Stream}.
 *
 * <p>Wraps a {@link UUID} to enforce non-null invariants and make ownership
 * semantics explicit in method signatures.
 */
public record UserId(UUID value) {

    /**
     * Compact constructor — validates that the wrapped value is not null.
     *
     * @param value the UUID to wrap; must not be null
     */
    public UserId {
        Objects.requireNonNull(value, "UserId value must not be null");
    }

    /**
     * Creates a {@code UserId} from an existing {@link UUID}.
     *
     * @param value the UUID; must not be null
     * @return a new {@code UserId}
     */
    public static UserId of(UUID value) {
        return new UserId(value);
    }
}
