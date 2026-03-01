package com.datastream.domain.model;

/**
 * Classifies the kind of data that flows through a {@link Stream}.
 */
public enum StreamType {

    /** General-purpose domain or integration events. */
    EVENT,

    /** Structured or unstructured log records. */
    LOG,

    /** Numeric measurements and time-series data. */
    METRIC,

    /** User-defined, application-specific data. */
    CUSTOM
}
