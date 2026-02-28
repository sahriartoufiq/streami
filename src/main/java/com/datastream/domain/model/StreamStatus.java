package com.datastream.domain.model;

/**
 * Represents the lifecycle status of a {@link Stream}.
 *
 * <p>Valid transitions:
 * <pre>
 *   DRAFT ──► ACTIVE ──► INACTIVE ──► ACTIVE
 *     │          │           │
 *     └──────────┴───────────┴──► DELETED
 * </pre>
 */
public enum StreamStatus {

    /** Newly created stream, not yet accepting data. */
    DRAFT,

    /** Stream is live and accepting/producing data. */
    ACTIVE,

    /** Stream is temporarily paused. */
    INACTIVE,

    /** Stream has been soft-deleted and is no longer accessible. */
    DELETED
}
