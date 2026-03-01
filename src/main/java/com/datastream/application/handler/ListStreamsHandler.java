package com.datastream.application.handler;

import com.datastream.application.dto.PagedResponse;
import com.datastream.application.dto.StreamResponse;
import com.datastream.application.mapper.StreamResponseMapper;
import com.datastream.application.query.ListStreamsQuery;
import com.datastream.domain.model.Page;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamFilter;
import com.datastream.domain.model.StreamStatus;
import com.datastream.domain.model.StreamType;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.valueobjects.UserId;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the {@link ListStreamsQuery} use case.
 *
 * <p>Translates optional string filter values to domain types, delegates the
 * query to the repository, and maps results to a {@link PagedResponse}.
 */
public class ListStreamsHandler {

    private final StreamRepository streamRepository;

    /**
     * Creates the handler with its required dependencies.
     *
     * @param streamRepository repository for reading stream data; must not be null
     */
    public ListStreamsHandler(StreamRepository streamRepository) {
        this.streamRepository = Objects.requireNonNull(streamRepository, "streamRepository must not be null");
    }

    /**
     * Executes the list-streams query.
     *
     * @param query the query carrying optional filters and pagination parameters; must not be null
     * @return a {@link PagedResponse} containing matching {@link StreamResponse} items
     */
    public PagedResponse<StreamResponse> handle(ListStreamsQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        UserId ownerId = query.ownerId() != null
                ? UserId.of(UUID.fromString(query.ownerId()))
                : null;
        StreamStatus status = query.status() != null
                ? StreamStatus.valueOf(query.status())
                : null;
        StreamType streamType = query.streamType() != null
                ? StreamType.valueOf(query.streamType())
                : null;

        StreamFilter filter = new StreamFilter(ownerId, status, streamType);
        Page<Stream> page = streamRepository.findAll(filter, query.page(), query.size());

        List<StreamResponse> content = page.content().stream()
                .map(StreamResponseMapper::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, page.pageNumber(), page.pageSize(),
                page.totalElements(), page.totalPages());
    }
}
