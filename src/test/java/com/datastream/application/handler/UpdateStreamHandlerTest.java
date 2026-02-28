package com.datastream.application.handler;

import com.datastream.application.command.UpdateStreamCommand;
import com.datastream.application.dto.StreamResponse;
import com.datastream.domain.exception.InvalidStreamOperationException;
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
class UpdateStreamHandlerTest {

    @Mock StreamDomainService streamDomainService;
    @Mock StreamRepository streamRepository;

    private UpdateStreamHandler handler;
    private StreamId streamId;
    private Stream draftStream;

    @BeforeEach
    void setUp() {
        handler = new UpdateStreamHandler(streamDomainService, streamRepository);
        streamId = StreamId.generate();
        draftStream = Stream.reconstitute(streamId, StreamName.of("Old Name"), "old desc",
                UserId.of(UUID.randomUUID()), StreamType.EVENT, StreamStatus.DRAFT,
                Instant.now(), Instant.now());
    }

    @Test
    void should_ReturnUpdatedStreamResponse_When_ValidCommandProvided() {
        UpdateStreamCommand command = new UpdateStreamCommand(streamId.value().toString(), "New Name", "new desc");
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(draftStream);
        when(streamRepository.save(any(Stream.class))).thenAnswer(inv -> inv.getArgument(0));

        StreamResponse response = handler.handle(command);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.description()).isEqualTo("new desc");
    }

    @Test
    void should_AllowNullDescription_When_UpdateCommandHasNullDescription() {
        UpdateStreamCommand command = new UpdateStreamCommand(streamId.value().toString(), "New Name", null);
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(draftStream);
        when(streamRepository.save(any(Stream.class))).thenAnswer(inv -> inv.getArgument(0));

        StreamResponse response = handler.handle(command);

        assertThat(response.description()).isNull();
    }

    @Test
    void should_ThrowStreamNotFoundException_When_StreamDoesNotExist() {
        UpdateStreamCommand command = new UpdateStreamCommand(streamId.value().toString(), "Name", null);
        doThrow(new StreamNotFoundException(streamId))
                .when(streamDomainService).getStreamOrThrow(streamId);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(StreamNotFoundException.class);
    }

    @Test
    void should_ThrowInvalidStreamOperationException_When_StreamIsDeleted() {
        Stream deletedStream = Stream.reconstitute(streamId, StreamName.of("Name"), null,
                UserId.of(UUID.randomUUID()), StreamType.EVENT, StreamStatus.DELETED,
                Instant.now(), Instant.now());
        UpdateStreamCommand command = new UpdateStreamCommand(streamId.value().toString(), "New Name", null);
        when(streamDomainService.getStreamOrThrow(streamId)).thenReturn(deletedStream);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InvalidStreamOperationException.class)
                .hasMessageContaining("DELETED");
    }

    @Test
    void should_ThrowNullPointerException_When_CommandIsNull() {
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(NullPointerException.class);
    }
}
