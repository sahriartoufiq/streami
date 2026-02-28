package com.datastream.domain.repository;

import com.datastream.domain.model.Page;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamFilter;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;

import java.util.Optional;

/**
 * Repository interface for {@link Stream} aggregate roots.
 *
 * <p>Defined in the domain layer and implemented in the infrastructure layer
 * (e.g. via a JPA adapter). The domain layer has no knowledge of the
 * underlying persistence technology.
 */
public interface StreamRepository {

    /**
     * Persists a stream (insert or update).
     *
     * @param stream the stream to persist; must not be null
     * @return the saved stream (may be a new instance with generated IDs)
     */
    Stream save(Stream stream);

    /**
     * Finds a stream by its unique identifier.
     *
     * @param streamId the ID to look up; must not be null
     * @return an {@link Optional} containing the stream, or empty if not found
     */
    Optional<Stream> findById(StreamId streamId);

    /**
     * Returns a paginated list of streams matching the given filter criteria.
     *
     * @param filter the optional filter criteria; use {@link StreamFilter#empty()} for no filtering
     * @param page   zero-based page index; must be >= 0
     * @param size   maximum number of results per page; must be > 0
     * @return a {@link Page} containing the matching streams
     */
    Page<Stream> findAll(StreamFilter filter, int page, int size);

    /**
     * Returns {@code true} if a stream with the given name already exists.
     *
     * @param streamName the name to check; must not be null
     * @return {@code true} if the name is taken, {@code false} otherwise
     */
    boolean existsByName(StreamName streamName);

    /**
     * Removes the stream with the given ID from the repository.
     *
     * <p>This is a hard delete at the repository level. Soft deletion is
     * handled in the domain via {@link Stream#softDelete()}.
     *
     * @param streamId the ID of the stream to remove; must not be null
     */
    void delete(StreamId streamId);
}
