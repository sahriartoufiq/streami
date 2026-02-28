package com.datastream.application.query;

/**
 * Query to retrieve a single stream by its unique identifier.
 *
 * @param streamId UUID string of the stream to retrieve; must not be null
 */
public record GetStreamQuery(String streamId) {
}
