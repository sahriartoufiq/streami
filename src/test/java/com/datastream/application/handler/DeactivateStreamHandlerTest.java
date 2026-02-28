package com.datastream.application.handler;

import com.datastream.application.command.DeactivateStreamCommand;
import com.datastream.domain.exception.InvalidStreamStateException;
import com.datastream.domain.exception.StreamNotFoundException;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamStatus;
import com.datastream.domain.model.StreamType;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.service.StreamDomainService;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.domain.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateStreamHandlerTest {

    @Mock StreamDomainService streamDomainService;
    @Mock StreamRepository streamRepository;

    private DeactivateStreamHandler handler;
    private StreamId streamId;

    @BeforeEach
    void setUp() {
        handler = new DeactivateStreamHandler(streamDomainService, streamRepository);
        streamId = StreamId.generate();
    }

    @Test
    void should_DeactivateStream_When_StreamIsActive() {
        Stream activeStream = Stream.reconstitute(streamId, StreamName.of("Stream"), null,
                UserId.of(UUID.randomUUID()), StreamType.EVENT, StreamStatus.ACTIVE,
                Instant.now(), Instant.now());
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(activeStream);
        when(streamRepository.save(any(Stream.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(
                () -> handler.handle(new DeactivateStreamCommand(streamId.value().toString())));

        verify(streamRepository).save(activeStream);
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_StreamIsDraft() {
        Stream draftStream = Stream.reconstitute(streamId, StreamName.of("Stream"), null,
                UserId.of(UUID.randomUUID()), StreamType.EVENT, StreamStatus.DRAFT,
                Instant.now(), Instant.now());
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(draftStream);

        assertThatThrownBy(() -> handler.handle(new DeactivateStreamCommand(streamId.value().toString())))
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_StreamIsAlreadyInactive() {
        Stream inactiveStream = Stream.reconstitute(streamId, StreamName.of("Stream"), null,
                UserId.of(UUID.randomUUID()), StreamType.EVENT, StreamStatus.INACTIVE,
                Instant.now(), Instant.now());
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(inactiveStream);

        assertThatThrownBy(() -> handler.handle(new DeactivateStreamCommand(streamId.value().toString())))
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("INACTIVE");
    }

    @Test
    void should_ThrowStreamNotFoundException_When_StreamDoesNotExist() {
        doThrow(new StreamNotFoundException(streamId))
                .when(streamDomainService).getStreamOrThrow(streamId);

        assertThatThrownBy(() -> handler.handle(new DeactivateStreamCommand(streamId.value().toString())))
                .isInstanceOf(StreamNotFoundException.class);
    }

    @Test
    void should_ThrowNullPointerException_When_CommandIsNull() {
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(NullPointerException.class);
    }
}
