package com.datastream.application.command;

/**
 * Command to update the name and description of an existing stream.
 *
 * @param streamId    UUID string of the stream to update; must not be null
 * @param name        the new stream name; must not be blank
 * @param description the new description; may be null
 */
public record UpdateStreamCommand(
        String streamId,
        String name,
        String description) {
}
