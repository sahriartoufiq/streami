package com.datastream.domain.model;

import com.datastream.domain.exception.InvalidStreamOperationException;
import com.datastream.domain.exception.InvalidStreamStateException;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.domain.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreamTest {

    private StreamName name;
    private UserId ownerId;

    @BeforeEach
    void setUp() {
        name = StreamName.of("Test Stream");
        ownerId = UserId.of(UUID.randomUUID());
    }

    // -------------------------------------------------------------------------
    // Factory: create
    // -------------------------------------------------------------------------

    @Test
    void should_CreateStreamWithDraftStatus_When_CreateCalled() {
        Stream stream = Stream.create(name, "description", ownerId, StreamType.EVENT);
        assertThat(stream.getStatus()).isEqualTo(StreamStatus.DRAFT);
    }

    @Test
    void should_AssignGeneratedId_When_CreateCalled() {
        Stream stream = Stream.create(name, "desc", ownerId, StreamType.LOG);
        assertThat(stream.getId()).isNotNull();
        assertThat(stream.getId().value()).isNotNull();
    }

    @Test
    void should_SetTimestamps_When_CreateCalled() {
        Instant before = Instant.now();
        Stream stream = Stream.create(name, "desc", ownerId, StreamType.METRIC);
        Instant after = Instant.now();

        assertThat(stream.getCreatedAt()).isBetween(before, after);
        assertThat(stream.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void should_SetAllFields_When_CreateCalled() {
        Stream stream = Stream.create(name, "my description", ownerId, StreamType.CUSTOM);
        assertThat(stream.getName()).isEqualTo(name);
        assertThat(stream.getDescription()).isEqualTo("my description");
        assertThat(stream.getOwnerId()).isEqualTo(ownerId);
        assertThat(stream.getStreamType()).isEqualTo(StreamType.CUSTOM);
    }

    @Test
    void should_AllowNullDescription_When_CreateCalled() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        assertThat(stream.getDescription()).isNull();
    }

    // -------------------------------------------------------------------------
    // activate()
    // -------------------------------------------------------------------------

    @Test
    void should_TransitionToActive_When_ActivateCalledFromDraft() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.activate();
        assertThat(stream.getStatus()).isEqualTo(StreamStatus.ACTIVE);
    }

    @Test
    void should_TransitionToActive_When_ActivateCalledFromInactive() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.activate();
        stream.deactivate();
        stream.activate();
        assertThat(stream.getStatus()).isEqualTo(StreamStatus.ACTIVE);
    }

    @Test
    void should_UpdateUpdatedAt_When_ActivateCalled() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        Instant before = Instant.now();
        stream.activate();
        assertThat(stream.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_ActivateCalledFromActive() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.activate();
        assertThatThrownBy(stream::activate)
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_ActivateCalledFromDeleted() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.softDelete();
        assertThatThrownBy(stream::activate)
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("DELETED");
    }

    // -------------------------------------------------------------------------
    // deactivate()
    // -------------------------------------------------------------------------

    @Test
    void should_TransitionToInactive_When_DeactivateCalledFromActive() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.activate();
        stream.deactivate();
        assertThat(stream.getStatus()).isEqualTo(StreamStatus.INACTIVE);
    }

    @Test
    void should_UpdateUpdatedAt_When_DeactivateCalled() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.activate();
        Instant before = Instant.now();
        stream.deactivate();
        assertThat(stream.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_DeactivateCalledFromDraft() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        assertThatThrownBy(stream::deactivate)
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_DeactivateCalledFromInactive() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.activate();
        stream.deactivate();
        assertThatThrownBy(stream::deactivate)
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("INACTIVE");
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_DeactivateCalledFromDeleted() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.softDelete();
        assertThatThrownBy(stream::deactivate)
                .isInstanceOf(InvalidStreamStateException.class);
    }

    // -------------------------------------------------------------------------
    // softDelete()
    // -------------------------------------------------------------------------

    @Test
    void should_TransitionToDeleted_When_SoftDeleteCalledFromDraft() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.softDelete();
        assertThat(stream.getStatus()).isEqualTo(StreamStatus.DELETED);
    }

    @Test
    void should_TransitionToDeleted_When_SoftDeleteCalledFromActive() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.activate();
        stream.softDelete();
        assertThat(stream.getStatus()).isEqualTo(StreamStatus.DELETED);
    }

    @Test
    void should_TransitionToDeleted_When_SoftDeleteCalledFromInactive() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.activate();
        stream.deactivate();
        stream.softDelete();
        assertThat(stream.getStatus()).isEqualTo(StreamStatus.DELETED);
    }

    @Test
    void should_UpdateUpdatedAt_When_SoftDeleteCalled() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        Instant before = Instant.now();
        stream.softDelete();
        assertThat(stream.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_SoftDeleteCalledFromDeleted() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.softDelete();
        assertThatThrownBy(stream::softDelete)
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("DELETED");
    }

    // -------------------------------------------------------------------------
    // updateConfig()
    // -------------------------------------------------------------------------

    @Test
    void should_UpdateNameAndDescription_When_UpdateConfigCalledOnDraftStream() {
        Stream stream = Stream.create(name, "old desc", ownerId, StreamType.EVENT);
        StreamName newName = StreamName.of("New Name");
        stream.updateConfig(newName, "new desc");
        assertThat(stream.getName()).isEqualTo(newName);
        assertThat(stream.getDescription()).isEqualTo("new desc");
    }

    @Test
    void should_UpdateNameAndDescription_When_UpdateConfigCalledOnActiveStream() {
        Stream stream = Stream.create(name, "old", ownerId, StreamType.LOG);
        stream.activate();
        StreamName newName = StreamName.of("Updated Name");
        stream.updateConfig(newName, "updated");
        assertThat(stream.getName()).isEqualTo(newName);
    }

    @Test
    void should_UpdateUpdatedAt_When_UpdateConfigCalled() {
        Stream stream = Stream.create(name, "desc", ownerId, StreamType.EVENT);
        Instant before = Instant.now();
        stream.updateConfig(StreamName.of("New Name"), "new");
        assertThat(stream.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void should_AllowNullDescription_When_UpdateConfigCalled() {
        Stream stream = Stream.create(name, "desc", ownerId, StreamType.EVENT);
        stream.updateConfig(StreamName.of("Name"), null);
        assertThat(stream.getDescription()).isNull();
    }

    @Test
    void should_ThrowInvalidStreamOperationException_When_UpdateConfigCalledOnDeletedStream() {
        Stream stream = Stream.create(name, null, ownerId, StreamType.EVENT);
        stream.softDelete();
        assertThatThrownBy(() -> stream.updateConfig(StreamName.of("New"), "desc"))
                .isInstanceOf(InvalidStreamOperationException.class)
                .hasMessageContaining("DELETED");
    }

    // -------------------------------------------------------------------------
    // reconstitute()
    // -------------------------------------------------------------------------

    @Test
    void should_ReconstitutStreamWithExactValues_When_ReconstituteCalledWithAllFields() {
        StreamId id = StreamId.generate();
        Instant created = Instant.parse("2024-01-01T00:00:00Z");
        Instant updated = Instant.parse("2024-06-01T00:00:00Z");

        Stream stream = Stream.reconstitute(id, name, "desc", ownerId,
                StreamType.METRIC, StreamStatus.ACTIVE, created, updated);

        assertThat(stream.getId()).isEqualTo(id);
        assertThat(stream.getStatus()).isEqualTo(StreamStatus.ACTIVE);
        assertThat(stream.getCreatedAt()).isEqualTo(created);
        assertThat(stream.getUpdatedAt()).isEqualTo(updated);
    }
}
