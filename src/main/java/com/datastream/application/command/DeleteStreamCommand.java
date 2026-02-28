package com.datastream.application.command;

/**
 * Command to soft-delete a stream (transitions status to DELETED).
 *
 * @param streamId UUID string of the stream to delete; must not be null
 */
public record DeleteStreamCommand(String streamId) {
}
