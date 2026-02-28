package com.datastream.application.handler;

import com.datastream.application.dto.StreamResponse;
import com.datastream.application.mapper.StreamResponseMapper;
import com.datastream.application.query.GetStreamQuery;
import com.datastream.domain.exception.StreamNotFoundException;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.valueobjects.StreamId;

import java.util.Objects;
import java.util.UUID;

/**
 * Handles the {@link GetStreamQuery} use case.
 *
 * <p>Loads a stream by ID from the repository and maps it to a {@link StreamResponse}.
 */
public class GetStreamHandler {

    private final StreamRepository streamRepository;

    /**
     * Creates the handler with its required dependencies.
     *
     * @param streamRepository repository for reading stream data; must not be null
     */
    public GetStreamHandler(StreamRepository streamRepository) {
        this.streamRepository = Objects.requireNonNull(streamRepository, "streamRepository must not be null");
    }

    /**
     * Executes the get-stream query.
     *
     * @param query the query carrying the stream ID; must not be null
     * @return a {@link StreamResponse} for the found stream
     * @throws StreamNotFoundException if no stream with the given ID exists
     */
    public StreamResponse handle(GetStreamQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        StreamId streamId = StreamId.of(UUID.fromString(query.streamId()));
        return streamRepository.findById(streamId)
                .map(StreamResponseMapper::toResponse)
                .orElseThrow(() -> new StreamNotFoundException(streamId));
    }
}
