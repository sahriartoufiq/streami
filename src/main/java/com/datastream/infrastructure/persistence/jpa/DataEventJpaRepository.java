package com.datastream.infrastructure.persistence.jpa;

import com.datastream.infrastructure.persistence.entity.DataEventJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link DataEventJpaEntity}.
 */
public interface DataEventJpaRepository extends JpaRepository<DataEventJpaEntity, UUID> {

    /**
     * Returns a paginated list of events for the given stream, ordered by
     * timestamp descending (newest first).
     *
     * @param streamId the stream UUID to query
     * @param pageable pagination and sort parameters
     * @return a page of matching events
     */
    Page<DataEventJpaEntity> findByStreamIdOrderByTimestampDesc(UUID streamId, Pageable pageable);
}
