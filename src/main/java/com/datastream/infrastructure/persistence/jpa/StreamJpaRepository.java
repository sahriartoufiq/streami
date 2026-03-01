package com.datastream.infrastructure.persistence.jpa;

import com.datastream.infrastructure.persistence.entity.StreamJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link StreamJpaEntity}.
 *
 * <p>Extends {@link JpaSpecificationExecutor} to support dynamic filtering
 * via {@link org.springframework.data.jpa.domain.Specification}.
 */
public interface StreamJpaRepository
        extends JpaRepository<StreamJpaEntity, UUID>,
                JpaSpecificationExecutor<StreamJpaEntity> {

    /**
     * Returns whether a stream with the given name exists.
     *
     * @param name the stream name to check
     * @return {@code true} if a stream with this name exists
     */
    boolean existsByName(String name);
}
