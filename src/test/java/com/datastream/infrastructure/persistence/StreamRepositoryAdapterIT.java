package com.datastream.infrastructure.persistence;

import com.datastream.domain.model.Page;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamFilter;
import com.datastream.domain.model.StreamStatus;
import com.datastream.domain.model.StreamType;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.domain.valueobjects.UserId;
import com.datastream.infrastructure.persistence.adapter.StreamRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link StreamRepositoryAdapter} against a real PostgreSQL instance
 * managed by Testcontainers. Each test rolls back via the inherited {@code @Transactional}.
 */
class StreamRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    StreamRepositoryAdapter streamRepositoryAdapter;

    private final UserId ownerId = UserId.of(UUID.randomUUID());

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Test
    void should_PersistAndReturnStream_When_SaveCalled() {
        Stream stream = Stream.create(StreamName.of("my-stream"), "desc", ownerId, StreamType.EVENT);

        Stream saved = streamRepositoryAdapter.save(stream);

        assertThat(saved.getId()).isEqualTo(stream.getId());
        assertThat(saved.getName().value()).isEqualTo("my-stream");
        assertThat(saved.getDescription()).isEqualTo("desc");
        assertThat(saved.getOwnerId()).isEqualTo(ownerId);
        assertThat(saved.getStreamType()).isEqualTo(StreamType.EVENT);
        assertThat(saved.getStatus()).isEqualTo(StreamStatus.DRAFT);
    }

    @Test
    void should_UpdateExistingStream_When_SaveCalledTwice() {
        Stream stream = Stream.create(StreamName.of("original"), null, ownerId, StreamType.LOG);
        streamRepositoryAdapter.save(stream);

        stream.activate();
        Stream updated = streamRepositoryAdapter.save(stream);

        assertThat(updated.getStatus()).isEqualTo(StreamStatus.ACTIVE);
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnStream_When_FindByIdCalledWithExistingId() {
        Stream stream = Stream.create(StreamName.of("find-me"), "desc", ownerId, StreamType.METRIC);
        streamRepositoryAdapter.save(stream);

        Optional<Stream> found = streamRepositoryAdapter.findById(stream.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(stream.getId());
        assertThat(found.get().getName().value()).isEqualTo("find-me");
        assertThat(found.get().getDescription()).isEqualTo("desc");
        assertThat(found.get().getStreamType()).isEqualTo(StreamType.METRIC);
        assertThat(found.get().getStatus()).isEqualTo(StreamStatus.DRAFT);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void should_ReturnEmpty_When_FindByIdCalledWithNonExistentId() {
        Optional<Stream> found = streamRepositoryAdapter.findById(StreamId.generate());

        assertThat(found).isEmpty();
    }

    @Test
    void should_PreserveAllFields_When_RoundTripThroughPersistence() {
        Stream original = Stream.create(StreamName.of("round-trip"), "description", ownerId, StreamType.CUSTOM);
        streamRepositoryAdapter.save(original);

        Stream reloaded = streamRepositoryAdapter.findById(original.getId()).orElseThrow();

        assertThat(reloaded.getId()).isEqualTo(original.getId());
        assertThat(reloaded.getName()).isEqualTo(original.getName());
        assertThat(reloaded.getDescription()).isEqualTo(original.getDescription());
        assertThat(reloaded.getOwnerId()).isEqualTo(original.getOwnerId());
        assertThat(reloaded.getStreamType()).isEqualTo(original.getStreamType());
        assertThat(reloaded.getStatus()).isEqualTo(original.getStatus());
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getUpdatedAt()).isNotNull();
    }

    @Test
    void should_PreserveNullDescription_When_DescriptionIsNull() {
        Stream stream = Stream.create(StreamName.of("no-desc"), null, ownerId, StreamType.EVENT);
        streamRepositoryAdapter.save(stream);

        Stream reloaded = streamRepositoryAdapter.findById(stream.getId()).orElseThrow();

        assertThat(reloaded.getDescription()).isNull();
    }

    // -------------------------------------------------------------------------
    // existsByName
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnTrue_When_StreamWithNameExists() {
        streamRepositoryAdapter.save(Stream.create(StreamName.of("taken-name"), null, ownerId, StreamType.EVENT));

        assertThat(streamRepositoryAdapter.existsByName(StreamName.of("taken-name"))).isTrue();
    }

    @Test
    void should_ReturnFalse_When_StreamWithNameDoesNotExist() {
        assertThat(streamRepositoryAdapter.existsByName(StreamName.of("no-such-stream"))).isFalse();
    }

    // -------------------------------------------------------------------------
    // findAll with filters
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnAllStreams_When_NoFilterApplied() {
        streamRepositoryAdapter.save(Stream.create(StreamName.of("s1"), null, ownerId, StreamType.EVENT));
        streamRepositoryAdapter.save(Stream.create(StreamName.of("s2"), null, ownerId, StreamType.LOG));

        Page<Stream> page = streamRepositoryAdapter.findAll(StreamFilter.empty(), 0, 10);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2L);
    }

    @Test
    void should_FilterByOwnerId_When_OwnerIdFilterApplied() {
        UserId otherOwner = UserId.of(UUID.randomUUID());
        streamRepositoryAdapter.save(Stream.create(StreamName.of("owner1-stream"), null, ownerId, StreamType.EVENT));
        streamRepositoryAdapter.save(Stream.create(StreamName.of("owner2-stream"), null, otherOwner, StreamType.EVENT));

        Page<Stream> page = streamRepositoryAdapter.findAll(new StreamFilter(ownerId, null, null), 0, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).getName().value()).isEqualTo("owner1-stream");
    }

    @Test
    void should_FilterByStatus_When_StatusFilterApplied() {
        Stream draft = Stream.create(StreamName.of("draft-stream"), null, ownerId, StreamType.EVENT);
        Stream active = Stream.create(StreamName.of("active-stream"), null, ownerId, StreamType.EVENT);
        active.activate();
        streamRepositoryAdapter.save(draft);
        streamRepositoryAdapter.save(active);

        Page<Stream> page = streamRepositoryAdapter.findAll(new StreamFilter(null, StreamStatus.ACTIVE, null), 0, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).getName().value()).isEqualTo("active-stream");
    }

    @Test
    void should_FilterByStreamType_When_StreamTypeFilterApplied() {
        streamRepositoryAdapter.save(Stream.create(StreamName.of("event-stream"), null, ownerId, StreamType.EVENT));
        streamRepositoryAdapter.save(Stream.create(StreamName.of("log-stream"), null, ownerId, StreamType.LOG));

        Page<Stream> page = streamRepositoryAdapter.findAll(new StreamFilter(null, null, StreamType.LOG), 0, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).getName().value()).isEqualTo("log-stream");
    }

    @Test
    void should_FilterByCombinedCriteria_When_MultipleFiltersApplied() {
        UserId otherOwner = UserId.of(UUID.randomUUID());
        Stream match = Stream.create(StreamName.of("match"), null, ownerId, StreamType.EVENT);
        match.activate();
        streamRepositoryAdapter.save(match);
        streamRepositoryAdapter.save(Stream.create(StreamName.of("wrong-owner"), null, otherOwner, StreamType.EVENT));
        streamRepositoryAdapter.save(Stream.create(StreamName.of("wrong-type"), null, ownerId, StreamType.LOG));

        Page<Stream> page = streamRepositoryAdapter.findAll(
                new StreamFilter(ownerId, StreamStatus.ACTIVE, StreamType.EVENT), 0, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).getName().value()).isEqualTo("match");
    }

    @Test
    void should_ReturnEmptyPage_When_NoStreamsMatchFilter() {
        streamRepositoryAdapter.save(Stream.create(StreamName.of("event-stream"), null, ownerId, StreamType.EVENT));

        Page<Stream> page = streamRepositoryAdapter.findAll(new StreamFilter(null, null, StreamType.METRIC), 0, 10);

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isEqualTo(0L);
    }

    // -------------------------------------------------------------------------
    // pagination
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnCorrectPage_When_PaginationApplied() {
        for (int i = 1; i <= 5; i++) {
            streamRepositoryAdapter.save(
                    Stream.create(StreamName.of("stream-" + i), null, ownerId, StreamType.EVENT));
        }

        Page<Stream> firstPage = streamRepositoryAdapter.findAll(StreamFilter.empty(), 0, 2);
        Page<Stream> secondPage = streamRepositoryAdapter.findAll(StreamFilter.empty(), 1, 2);
        Page<Stream> thirdPage = streamRepositoryAdapter.findAll(StreamFilter.empty(), 2, 2);

        assertThat(firstPage.content()).hasSize(2);
        assertThat(secondPage.content()).hasSize(2);
        assertThat(thirdPage.content()).hasSize(1);
        assertThat(firstPage.totalElements()).isEqualTo(5L);
        assertThat(firstPage.totalPages()).isEqualTo(3);
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void should_RemoveStream_When_DeleteCalled() {
        Stream stream = Stream.create(StreamName.of("to-delete"), null, ownerId, StreamType.EVENT);
        streamRepositoryAdapter.save(stream);

        streamRepositoryAdapter.delete(stream.getId());

        assertThat(streamRepositoryAdapter.findById(stream.getId())).isEmpty();
    }
}
