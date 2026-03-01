package com.datastream.domain.model;

import com.datastream.domain.valueobjects.EventPayload;
import com.datastream.domain.valueobjects.StreamId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataEventTest {

    private final StreamId streamId = StreamId.generate();
    private final EventPayload payload = EventPayload.of("test payload".getBytes());

    @Test
    void should_CreateDataEvent_When_ValidArgumentsProvided() {
        DataEvent event = DataEvent.create(streamId, payload, Collections.emptyMap());
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getStreamId()).isEqualTo(streamId);
        assertThat(event.getPayload()).isEqualTo(payload);
        assertThat(event.getMetadata()).isEmpty();
    }

    @Test
    void should_SetTimestampToNow_When_CreateCalled() {
        Instant before = Instant.now();
        DataEvent event = DataEvent.create(streamId, payload, Collections.emptyMap());
        Instant after = Instant.now();
        assertThat(event.getTimestamp()).isBetween(before, after);
    }

    @Test
    void should_GenerateUniqueEventIds_When_MultipleEventsCreated() {
        DataEvent first = DataEvent.create(streamId, payload, Collections.emptyMap());
        DataEvent second = DataEvent.create(streamId, payload, Collections.emptyMap());
        assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
    }

    @Test
    void should_StoreMetadata_When_MetadataProvided() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("source", "sensor-01");
        metadata.put("region", "us-east-1");

        DataEvent event = DataEvent.create(streamId, payload, metadata);

        assertThat(event.getMetadata())
                .containsEntry("source", "sensor-01")
                .containsEntry("region", "us-east-1");
    }

    @Test
    void should_ReturnUnmodifiableMetadata_When_GetMetadataCalled() {
        DataEvent event = DataEvent.create(streamId, payload, new HashMap<>());
        assertThatThrownBy(() -> event.getMetadata().put("key", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_ThrowNullPointerException_When_StreamIdIsNull() {
        assertThatThrownBy(() -> DataEvent.create(null, payload, Collections.emptyMap()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("streamId");
    }

    @Test
    void should_ThrowNullPointerException_When_PayloadIsNull() {
        assertThatThrownBy(() -> DataEvent.create(streamId, null, Collections.emptyMap()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("payload");
    }

    @Test
    void should_ThrowNullPointerException_When_MetadataIsNull() {
        assertThatThrownBy(() -> DataEvent.create(streamId, payload, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("metadata");
    }

    @Test
    void should_ReconstituteWithExactValues_When_ReconstituteCalledWithAllFields() {
        UUID eventId = UUID.randomUUID();
        Instant timestamp = Instant.parse("2024-03-01T10:00:00Z");
        Map<String, String> metadata = Map.of("k", "v");

        DataEvent event = DataEvent.reconstitute(eventId, streamId, payload, metadata, timestamp);

        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getStreamId()).isEqualTo(streamId);
        assertThat(event.getPayload()).isEqualTo(payload);
        assertThat(event.getMetadata()).containsEntry("k", "v");
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
    }
}
