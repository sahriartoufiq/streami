package com.datastream.application.command;

import java.util.Map;

/**
 * Command to publish a data event to a stream.
 *
 * @param streamId UUID string of the target stream; must not be null
 * @param payload  raw binary payload; must not be null, max 1 MB
 * @param metadata arbitrary string key/value metadata; must not be null
 */
public record PublishEventCommand(
        String streamId,
        byte[] payload,
        Map<String, String> metadata) {
}
