package com.datastream.domain.service;

import com.datastream.domain.exception.InvalidStreamStateException;
import com.datastream.domain.exception.StreamAlreadyExistsException;
import com.datastream.domain.exception.StreamNotFoundException;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamStatus;
import com.datastream.domain.model.StreamType;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.domain.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamDomainServiceTest {

    @Mock
    private StreamRepository streamRepository;

    private StreamDomainService streamDomainService;

    private StreamId streamId;
    private StreamName streamName;
    private UserId ownerId;

    @BeforeEach
    void setUp() {
        streamDomainService = new StreamDomainService(streamRepository);
        streamId = StreamId.generate();
        streamName = StreamName.of("My Stream");
        ownerId = UserId.of(UUID.randomUUID());
    }

    // -------------------------------------------------------------------------
    // constructor
    // -------------------------------------------------------------------------

    @Test
    void should_ThrowNullPointerException_When_RepositoryIsNull() {
        assertThatThrownBy(() -> new StreamDomainService(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("streamRepository");
    }

    // -------------------------------------------------------------------------
    // validateStreamNameUnique
    // -------------------------------------------------------------------------

    @Test
    void should_NotThrow_When_StreamNameDoesNotExist() {
        when(streamRepository.existsByName(streamName)).thenReturn(false);
        // should not throw
        streamDomainService.validateStreamNameUnique(streamName);
    }

    @Test
    void should_ThrowStreamAlreadyExistsException_When_StreamNameAlreadyExists() {
        when(streamRepository.existsByName(streamName)).thenReturn(true);
        assertThatThrownBy(() -> streamDomainService.validateStreamNameUnique(streamName))
                .isInstanceOf(StreamAlreadyExistsException.class)
                .hasMessageContaining("My Stream");
    }

    @Test
    void should_ThrowNullPointerException_When_StreamNameIsNull() {
        assertThatThrownBy(() -> streamDomainService.validateStreamNameUnique(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("streamName");
    }

    // -------------------------------------------------------------------------
    // validateStreamIsActive
    // -------------------------------------------------------------------------

    @Test
    void should_NotThrow_When_StreamIsActive() {
        Stream activeStream = activeStream();
        when(streamRepository.findById(streamId)).thenReturn(Optional.of(activeStream));
        // should not throw
        streamDomainService.validateStreamIsActive(streamId);
    }

    @Test
    void should_ThrowStreamNotFoundException_When_StreamDoesNotExistOnActiveCheck() {
        when(streamRepository.findById(streamId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> streamDomainService.validateStreamIsActive(streamId))
                .isInstanceOf(StreamNotFoundException.class)
                .hasMessageContaining(streamId.value().toString());
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_StreamIsDraftOnActiveCheck() {
        Stream draftStream = draftStream();
        when(streamRepository.findById(streamId)).thenReturn(Optional.of(draftStream));
        assertThatThrownBy(() -> streamDomainService.validateStreamIsActive(streamId))
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_StreamIsInactiveOnActiveCheck() {
        Stream inactiveStream = inactiveStream();
        when(streamRepository.findById(streamId)).thenReturn(Optional.of(inactiveStream));
        assertThatThrownBy(() -> streamDomainService.validateStreamIsActive(streamId))
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("INACTIVE");
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_StreamIsDeletedOnActiveCheck() {
        Stream deletedStream = deletedStream();
        when(streamRepository.findById(streamId)).thenReturn(Optional.of(deletedStream));
        assertThatThrownBy(() -> streamDomainService.validateStreamIsActive(streamId))
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("DELETED");
    }

    // -------------------------------------------------------------------------
    // getStreamOrThrow
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnStream_When_StreamExists() {
        Stream stream = draftStream();
        when(streamRepository.findById(streamId)).thenReturn(Optional.of(stream));
        Stream result = streamDomainService.getStreamOrThrow(streamId);
        assertThat(result).isSameAs(stream);
    }

    @Test
    void should_ThrowStreamNotFoundException_When_StreamDoesNotExist() {
        when(streamRepository.findById(streamId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> streamDomainService.getStreamOrThrow(streamId))
                .isInstanceOf(StreamNotFoundException.class)
                .hasMessageContaining(streamId.value().toString());
    }

    @Test
    void should_ThrowNullPointerException_When_StreamIdIsNull() {
        assertThatThrownBy(() -> streamDomainService.getStreamOrThrow(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("streamId");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Stream draftStream() {
        return Stream.reconstitute(streamId, streamName, null, ownerId,
                StreamType.EVENT, StreamStatus.DRAFT,
                java.time.Instant.now(), java.time.Instant.now());
    }

    private Stream activeStream() {
        return Stream.reconstitute(streamId, streamName, null, ownerId,
                StreamType.EVENT, StreamStatus.ACTIVE,
                java.time.Instant.now(), java.time.Instant.now());
    }

    private Stream inactiveStream() {
        return Stream.reconstitute(streamId, streamName, null, ownerId,
                StreamType.EVENT, StreamStatus.INACTIVE,
                java.time.Instant.now(), java.time.Instant.now());
    }

    private Stream deletedStream() {
        return Stream.reconstitute(streamId, streamName, null, ownerId,
                StreamType.EVENT, StreamStatus.DELETED,
                java.time.Instant.now(), java.time.Instant.now());
    }
}
