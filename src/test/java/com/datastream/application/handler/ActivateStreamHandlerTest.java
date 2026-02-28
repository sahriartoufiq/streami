package com.datastream.application.handler;

import com.datastream.application.command.ActivateStreamCommand;
import com.datastream.application.dto.StreamResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateStreamHandlerTest {

    @Mock StreamDomainService streamDomainService;
    @Mock StreamRepository streamRepository;

    private ActivateStreamHandler handler;
    private StreamId streamId;

    @BeforeEach
    void setUp() {
        handler = new ActivateStreamHandler(streamDomainService, streamRepository);
        streamId = StreamId.generate();
    }

    @Test
    void should_ReturnActiveStreamResponse_When_ActivatingDraftStream() {
        Stream draftStream = Stream.reconstitute(streamId, StreamName.of("Stream"), null,
                UserId.of(UUID.randomUUID()), StreamType.EVENT, StreamStatus.DRAFT,
                Instant.now(), Instant.now());
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(draftStream);
        when(streamRepository.save(any(Stream.class))).thenAnswer(inv -> inv.getArgument(0));

        StreamResponse response = handler.handle(new ActivateStreamCommand(streamId.value().toString()));

        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void should_ReturnActiveStreamResponse_When_ActivatingInactiveStream() {
        Stream inactiveStream = Stream.reconstitute(streamId, StreamName.of("Stream"), null,
                UserId.of(UUID.randomUUID()), StreamType.LOG, StreamStatus.INACTIVE,
                Instant.now(), Instant.now());
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(inactiveStream);
        when(streamRepository.save(any(Stream.class))).thenAnswer(inv -> inv.getArgument(0));

        StreamResponse response = handler.handle(new ActivateStreamCommand(streamId.value().toString()));

        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_StreamIsAlreadyActive() {
        Stream activeStream = Stream.reconstitute(streamId, StreamName.of("Stream"), null,
                UserId.of(UUID.randomUUID()), StreamType.EVENT, StreamStatus.ACTIVE,
                Instant.now(), Instant.now());
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(activeStream);

        assertThatThrownBy(() -> handler.handle(new ActivateStreamCommand(streamId.value().toString())))
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_StreamIsDeleted() {
        Stream deletedStream = Stream.reconstitute(streamId, StreamName.of("Stream"), null,
                UserId.of(UUID.randomUUID()), StreamType.EVENT, StreamStatus.DELETED,
                Instant.now(), Instant.now());
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(deletedStream);

        assertThatThrownBy(() -> handler.handle(new ActivateStreamCommand(streamId.value().toString())))
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("DELETED");
    }

    @Test
    void should_ThrowStreamNotFoundException_When_StreamDoesNotExist() {
        doThrow(new StreamNotFoundException(streamId))
                .when(streamDomainService).getStreamOrThrow(streamId);

        assertThatThrownBy(() -> handler.handle(new ActivateStreamCommand(streamId.value().toString())))
                .isInstanceOf(StreamNotFoundException.class);
    }

    @Test
    void should_ThrowNullPointerException_When_CommandIsNull() {
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(NullPointerException.class);
    }
}
