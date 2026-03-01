package com.datastream.infrastructure.persistence.adapter;

import com.datastream.domain.model.DataEvent;
import com.datastream.domain.model.Page;
import com.datastream.domain.repository.DataEventRepository;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.infrastructure.persistence.entity.DataEventJpaEntity;
import com.datastream.infrastructure.persistence.jpa.DataEventJpaRepository;
import com.datastream.infrastructure.persistence.mapper.DataEventEntityMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter implementing {@link DataEventRepository} via Spring Data JPA.
 *
 * <p>Translates between the domain {@link DataEvent} entity and the
 * {@link DataEventJpaEntity} JPA entity using {@link DataEventEntityMapper}.
 */
@Repository
@Transactional
public class DataEventRepositoryAdapter implements DataEventRepository {

    private final DataEventJpaRepository dataEventJpaRepository;

    /**
     * Creates the adapter with its required JPA repository dependency.
     *
     * @param dataEventJpaRepository the underlying Spring Data repository; must not be null
     */
    public DataEventRepositoryAdapter(DataEventJpaRepository dataEventJpaRepository) {
        this.dataEventJpaRepository = Objects.requireNonNull(
                dataEventJpaRepository, "dataEventJpaRepository must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataEvent save(DataEvent dataEvent) {
        DataEventJpaEntity entity = DataEventEntityMapper.toJpaEntity(dataEvent);
        DataEventJpaEntity saved = dataEventJpaRepository.save(entity);
        return DataEventEntityMapper.toDomain(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DataEvent> findByStreamId(StreamId streamId, int page, int size) {
        org.springframework.data.domain.Page<DataEventJpaEntity> result =
                dataEventJpaRepository.findByStreamIdOrderByTimestampDesc(
                        streamId.value(), PageRequest.of(page, size));

        List<DataEvent> content = result.getContent().stream()
                .map(DataEventEntityMapper::toDomain)
                .collect(Collectors.toList());

        return new Page<>(content, page, size, result.getTotalElements());
    }
}
