package com.datastream.infrastructure.persistence.specification;

import com.datastream.domain.model.StreamFilter;
import com.datastream.infrastructure.persistence.entity.StreamJpaEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Factory for {@link Specification} instances used when querying the {@code streams} table.
 *
 * <p>All methods are static utilities; this class is not intended to be instantiated.
 */
public final class StreamSpecifications {

    private StreamSpecifications() {
    }

    /**
     * Filters streams by owner ID.
     *
     * @param ownerId the UUID of the owning user
     * @return a {@link Specification} matching streams with the given owner
     */
    public static Specification<StreamJpaEntity> withOwnerId(UUID ownerId) {
        return (root, query, cb) -> cb.equal(root.get("ownerId"), ownerId);
    }

    /**
     * Filters streams by status string.
     *
     * @param status the status value (e.g. {@code "ACTIVE"})
     * @return a {@link Specification} matching streams with the given status
     */
    public static Specification<StreamJpaEntity> withStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Filters streams by stream type string.
     *
     * @param streamType the stream type value (e.g. {@code "EVENT"})
     * @return a {@link Specification} matching streams with the given type
     */
    public static Specification<StreamJpaEntity> withStreamType(String streamType) {
        return (root, query, cb) -> cb.equal(root.get("streamType"), streamType);
    }

    /**
     * Builds a combined {@link Specification} from a {@link StreamFilter}.
     *
     * <p>Only non-null filter fields contribute a predicate; a null field means
     * "no restriction on this dimension".
     *
     * @param filter the filter criteria; must not be null
     * @return a composed {@link Specification}, possibly empty (matches all rows)
     */
    public static Specification<StreamJpaEntity> fromFilter(StreamFilter filter) {
        Specification<StreamJpaEntity> spec = Specification.where(null);

        if (filter.ownerId() != null) {
            spec = spec.and(withOwnerId(filter.ownerId().value()));
        }
        if (filter.status() != null) {
            spec = spec.and(withStatus(filter.status().name()));
        }
        if (filter.streamType() != null) {
            spec = spec.and(withStreamType(filter.streamType().name()));
        }
        return spec;
    }
}
