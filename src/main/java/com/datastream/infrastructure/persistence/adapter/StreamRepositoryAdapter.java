package com.datastream.infrastructure.persistence.adapter;

import com.datastream.domain.model.Page;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamFilter;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.infrastructure.persistence.entity.StreamJpaEntity;
import com.datastream.infrastructure.persistence.jpa.StreamJpaRepository;
import com.datastream.infrastructure.persistence.mapper.StreamEntityMapper;
import com.datastream.infrastructure.persistence.specification.StreamSpecifications;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter implementing {@link StreamRepository} via Spring Data JPA.
 *
 * <p>Translates between the domain {@link Stream} aggregate and the
 * {@link StreamJpaEntity} JPA entity using {@link StreamEntityMapper}.
 */
@Repository
@Transactional
public class StreamRepositoryAdapter implements StreamRepository {

    private final StreamJpaRepository streamJpaRepository;

    /**
     * Creates the adapter with its required JPA repository dependency.
     *
     * @param streamJpaRepository the underlying Spring Data repository; must not be null
     */
    public StreamRepositoryAdapter(StreamJpaRepository streamJpaRepository) {
        this.streamJpaRepository = Objects.requireNonNull(
                streamJpaRepository, "streamJpaRepository must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream save(Stream stream) {
        StreamJpaEntity entity = StreamEntityMapper.toJpaEntity(stream);
        StreamJpaEntity saved = streamJpaRepository.save(entity);
        return StreamEntityMapper.toDomain(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Stream> findById(StreamId streamId) {
        return streamJpaRepository.findById(streamId.value())
                .map(StreamEntityMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Stream> findAll(StreamFilter filter, int page, int size) {
        Specification<StreamJpaEntity> spec = StreamSpecifications.fromFilter(filter);
        org.springframework.data.domain.Page<StreamJpaEntity> result =
                streamJpaRepository.findAll(spec, PageRequest.of(page, size));

        List<Stream> content = result.getContent().stream()
                .map(StreamEntityMapper::toDomain)
                .collect(Collectors.toList());

        return new Page<>(content, page, size, result.getTotalElements());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(StreamName streamName) {
        return streamJpaRepository.existsByName(streamName.value());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(StreamId streamId) {
        streamJpaRepository.deleteById(streamId.value());
    }
}
