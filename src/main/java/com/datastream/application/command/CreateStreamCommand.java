package com.datastream.application.command;

/**
 * Command to create a new data stream configuration.
 *
 * @param name        the stream name; must not be blank
 * @param description optional human-readable description; may be null
 * @param ownerId     UUID string of the owning user; must not be null
 * @param streamType  name of the {@link com.datastream.domain.model.StreamType} enum value
 */
public record CreateStreamCommand(
        String name,
        String description,
        String ownerId,
        String streamType) {
}
