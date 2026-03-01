package com.datastream.domain.valueobjects;

import java.util.Arrays;
import java.util.Objects;

/**
 * Value object wrapping the raw binary payload of a {@link com.datastream.domain.model.DataEvent}.
 *
 * <p>Enforces the following invariants at construction time:
 * <ul>
 *   <li>Must not be null</li>
 *   <li>Must not exceed 1 MB (1,048,576 bytes)</li>
 * </ul>
 * A defensive copy of the byte array is stored to preserve immutability semantics.
 */
public record EventPayload(byte[] value) {

    /** Maximum allowed payload size: 1 MB. */
    static final int MAX_SIZE_BYTES = 1024 * 1024;

    /**
     * Compact constructor — validates invariants and stores a defensive copy.
     *
     * @param value the payload bytes; must not be null or exceed 1 MB
     */
    public EventPayload {
        Objects.requireNonNull(value, "EventPayload must not be null");
        if (value.length > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "EventPayload must not exceed 1 MB (" + MAX_SIZE_BYTES + " bytes), got " + value.length);
        }
        value = Arrays.copyOf(value, value.length);
    }

    /**
     * Returns a defensive copy of the payload bytes.
     *
     * @return copy of the raw bytes
     */
    @Override
    public byte[] value() {
        return Arrays.copyOf(value, value.length);
    }

    /**
     * Creates an {@code EventPayload} from the given byte array.
     *
     * @param value the payload bytes; must not be null or exceed 1 MB
     * @return a new {@code EventPayload}
     */
    public static EventPayload of(byte[] value) {
        return new EventPayload(value);
    }

    /**
     * Two {@code EventPayload} instances are equal when their byte contents are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EventPayload other)) return false;
        return Arrays.equals(value, other.value);
    }

    /** Hash code consistent with {@link #equals}. */
    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    /** Returns the payload size in bytes for diagnostic purposes. */
    @Override
    public String toString() {
        return "EventPayload[size=" + value.length + "]";
    }
}
