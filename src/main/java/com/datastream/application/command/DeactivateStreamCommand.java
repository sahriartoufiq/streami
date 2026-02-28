package com.datastream.application.command;

/**
 * Command to deactivate a stream (transitions status to INACTIVE from ACTIVE).
 *
 * @param streamId UUID string of the stream to deactivate; must not be null
 */
public record DeactivateStreamCommand(String streamId) {
}
