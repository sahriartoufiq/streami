package com.datastream.domain.valueobjects;

import java.util.Objects;

/**
 * Value object representing the human-readable name of a {@code Stream}.
 *
 * <p>Enforces the following invariants at construction time:
 * <ul>
 *   <li>Must not be null or blank</li>
 *   <li>Trimmed value must not exceed 255 characters</li>
 * </ul>
 * The stored value is always the trimmed form of the input.
 */
public record StreamName(String value) {

    private static final int MAX_LENGTH = 255;

    /**
     * Compact constructor — trims the input and validates invariants.
     *
     * @param value the name; must not be null, blank, or exceed 255 chars after trimming
     */
    public StreamName {
        Objects.requireNonNull(value, "StreamName must not be null");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("StreamName must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "StreamName must not exceed " + MAX_LENGTH + " characters, got " + value.length());
        }
    }

    /**
     * Creates a {@code StreamName} from the given string.
     *
     * @param value the name string; must not be null or blank, max 255 chars
     * @return a new {@code StreamName} with the trimmed value
     */
    public static StreamName of(String value) {
        return new StreamName(value);
    }
}
