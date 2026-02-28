package com.datastream.domain.repository;

import com.datastream.domain.model.DataEvent;
import com.datastream.domain.model.Page;
import com.datastream.domain.valueobjects.StreamId;

/**
 * Repository interface for {@link DataEvent} entities.
 *
 * <p>Defined in the domain layer and implemented in the infrastructure layer.
 */
public interface DataEventRepository {

    /**
     * Persists a data event.
     *
     * @param dataEvent the event to persist; must not be null
     * @return the saved event
     */
    DataEvent save(DataEvent dataEvent);

    /**
     * Returns a paginated list of events belonging to the given stream,
     * ordered by timestamp ascending.
     *
     * @param streamId the stream to query; must not be null
     * @param page     zero-based page index; must be >= 0
     * @param size     maximum number of results per page; must be > 0
     * @return a {@link Page} of {@link DataEvent} for the given stream
     */
    Page<DataEvent> findByStreamId(StreamId streamId, int page, int size);
}
