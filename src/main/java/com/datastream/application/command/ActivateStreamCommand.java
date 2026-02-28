package com.datastream.application.command;

/**
 * Command to activate a stream (transitions status to ACTIVE from DRAFT or INACTIVE).
 *
 * @param streamId UUID string of the stream to activate; must not be null
 */
public record ActivateStreamCommand(String streamId) {
}
