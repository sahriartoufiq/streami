package com.datastream.infrastructure.persistence;

import com.datastream.domain.model.DataEvent;
import com.datastream.domain.model.Page;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamType;
import com.datastream.domain.valueobjects.EventPayload;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.domain.valueobjects.UserId;
import com.datastream.infrastructure.persistence.adapter.DataEventRepositoryAdapter;
import com.datastream.infrastructure.persistence.adapter.StreamRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DataEventRepositoryAdapter} against a real PostgreSQL instance.
 * Each test rolls back via the inherited {@code @Transactional}.
 */
class DataEventRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    DataEventRepositoryAdapter dataEventRepositoryAdapter;

    @Autowired
    StreamRepositoryAdapter streamRepositoryAdapter;

    private StreamId streamId;

    @BeforeEach
    void setUp() {
        Stream stream = Stream.create(
                StreamName.of("test-stream-" + UUID.randomUUID()),
                null,
                UserId.of(UUID.randomUUID()),
                StreamType.EVENT);
        stream.activate();
        Stream saved = streamRepositoryAdapter.save(stream);
        streamId = saved.getId();
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Test
    void should_PersistAndReturnDataEvent_When_SaveCalled() {
        EventPayload payload = EventPayload.of("hello".getBytes());
        DataEvent event = DataEvent.create(streamId, payload, Collections.emptyMap());

        DataEvent saved = dataEventRepositoryAdapter.save(event);

        assertThat(saved.getEventId()).isEqualTo(event.getEventId());
        assertThat(saved.getStreamId()).isEqualTo(streamId);
        assertThat(saved.getPayload().value()).isEqualTo("hello".getBytes());
        assertThat(saved.getMetadata()).isEmpty();
        assertThat(saved.getTimestamp()).isNotNull();
    }

    @Test
    void should_PersistMetadata_When_EventHasMetadata() {
        Map<String, String> metadata = Map.of("source", "sensor-01", "region", "eu-west-1");
        DataEvent event = DataEvent.create(streamId, EventPayload.of("data".getBytes()), metadata);

        DataEvent saved = dataEventRepositoryAdapter.save(event);

        assertThat(saved.getMetadata())
                .containsEntry("source", "sensor-01")
                .containsEntry("region", "eu-west-1");
    }

    @Test
    void should_PreserveAllFields_When_RoundTripThroughPersistence() {
        EventPayload payload = EventPayload.of(new byte[]{1, 2, 3, 4, 5});
        Map<String, String> metadata = Map.of("key", "value");
        DataEvent original = DataEvent.create(streamId, payload, metadata);
        dataEventRepositoryAdapter.save(original);

        Page<DataEvent> page = dataEventRepositoryAdapter.findByStreamId(streamId, 0, 10);
        DataEvent reloaded = page.content().get(0);

        assertThat(reloaded.getEventId()).isEqualTo(original.getEventId());
        assertThat(reloaded.getStreamId()).isEqualTo(original.getStreamId());
        assertThat(reloaded.getPayload().value()).isEqualTo(payload.value());
        assertThat(reloaded.getMetadata()).containsEntry("key", "value");
        assertThat(reloaded.getTimestamp()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // findByStreamId
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnEventsForStream_When_FindByStreamIdCalled() {
        dataEventRepositoryAdapter.save(DataEvent.create(streamId, EventPayload.of("e1".getBytes()), Collections.emptyMap()));
        dataEventRepositoryAdapter.save(DataEvent.create(streamId, EventPayload.of("e2".getBytes()), Collections.emptyMap()));
        dataEventRepositoryAdapter.save(DataEvent.create(streamId, EventPayload.of("e3".getBytes()), Collections.emptyMap()));

        Page<DataEvent> page = dataEventRepositoryAdapter.findByStreamId(streamId, 0, 10);

        assertThat(page.content()).hasSize(3);
        assertThat(page.totalElements()).isEqualTo(3L);
    }

    @Test
    void should_ReturnEmptyPage_When_NoEventsForStream() {
        Page<DataEvent> page = dataEventRepositoryAdapter.findByStreamId(streamId, 0, 10);

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isEqualTo(0L);
    }

    @Test
    void should_NotReturnEventsOfOtherStreams_When_FindByStreamIdCalled() {
        Stream otherStream = Stream.create(
                StreamName.of("other-" + UUID.randomUUID()), null,
                UserId.of(UUID.randomUUID()), StreamType.LOG);
        otherStream.activate();
        StreamId otherStreamId = streamRepositoryAdapter.save(otherStream).getId();

        dataEventRepositoryAdapter.save(DataEvent.create(streamId, EventPayload.of("mine".getBytes()), Collections.emptyMap()));
        dataEventRepositoryAdapter.save(DataEvent.create(otherStreamId, EventPayload.of("theirs".getBytes()), Collections.emptyMap()));

        Page<DataEvent> myPage = dataEventRepositoryAdapter.findByStreamId(streamId, 0, 10);
        Page<DataEvent> theirPage = dataEventRepositoryAdapter.findByStreamId(otherStreamId, 0, 10);

        assertThat(myPage.content()).hasSize(1);
        assertThat(theirPage.content()).hasSize(1);
    }

    @Test
    void should_ReturnEventsNewestFirst_When_MultipleEventsExist() throws InterruptedException {
        // Insert with small delays to ensure distinct timestamps
        DataEvent first = DataEvent.create(streamId, EventPayload.of("first".getBytes()), Collections.emptyMap());
        dataEventRepositoryAdapter.save(first);
        Thread.sleep(5);
        DataEvent second = DataEvent.create(streamId, EventPayload.of("second".getBytes()), Collections.emptyMap());
        dataEventRepositoryAdapter.save(second);
        Thread.sleep(5);
        DataEvent third = DataEvent.create(streamId, EventPayload.of("third".getBytes()), Collections.emptyMap());
        dataEventRepositoryAdapter.save(third);

        Page<DataEvent> page = dataEventRepositoryAdapter.findByStreamId(streamId, 0, 10);

        assertThat(page.content()).hasSize(3);
        // Newest first (DESC order)
        assertThat(page.content().get(0).getTimestamp())
                .isAfterOrEqualTo(page.content().get(1).getTimestamp());
        assertThat(page.content().get(1).getTimestamp())
                .isAfterOrEqualTo(page.content().get(2).getTimestamp());
    }

    @Test
    void should_ApplyPagination_When_ManyEventsExist() {
        for (int i = 0; i < 5; i++) {
            dataEventRepositoryAdapter.save(DataEvent.create(
                    streamId, EventPayload.of(("event-" + i).getBytes()), Collections.emptyMap()));
        }

        Page<DataEvent> firstPage = dataEventRepositoryAdapter.findByStreamId(streamId, 0, 2);
        Page<DataEvent> secondPage = dataEventRepositoryAdapter.findByStreamId(streamId, 1, 2);
        Page<DataEvent> thirdPage = dataEventRepositoryAdapter.findByStreamId(streamId, 2, 2);

        assertThat(firstPage.content()).hasSize(2);
        assertThat(secondPage.content()).hasSize(2);
        assertThat(thirdPage.content()).hasSize(1);
        assertThat(firstPage.totalElements()).isEqualTo(5L);
        assertThat(firstPage.totalPages()).isEqualTo(3);
    }
}
