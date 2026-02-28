package com.datastream.application.handler;

import com.datastream.application.command.CreateStreamCommand;
import com.datastream.application.dto.StreamResponse;
import com.datastream.domain.exception.StreamAlreadyExistsException;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamStatus;
import com.datastream.domain.model.StreamType;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.service.StreamDomainService;
import com.datastream.domain.valueobjects.StreamName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateStreamHandlerTest {

    @Mock StreamDomainService streamDomainService;
    @Mock StreamRepository streamRepository;

    private CreateStreamHandler handler;
    private final String ownerId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        handler = new CreateStreamHandler(streamDomainService, streamRepository);
    }

    @Test
    void should_ReturnStreamResponse_When_ValidCommandProvided() {
        CreateStreamCommand command = new CreateStreamCommand("My Stream", "desc", ownerId, "EVENT");
        when(streamRepository.save(any(Stream.class))).thenAnswer(inv -> inv.getArgument(0));

        StreamResponse response = handler.handle(command);

        assertThat(response.name()).isEqualTo("My Stream");
        assertThat(response.description()).isEqualTo("desc");
        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.streamType()).isEqualTo("EVENT");
        assertThat(response.status()).isEqualTo("DRAFT");
    }

    @Test
    void should_PersistStreamWithCorrectFields_When_ValidCommandProvided() {
        CreateStreamCommand command = new CreateStreamCommand("Events", null, ownerId, "LOG");
        when(streamRepository.save(any(Stream.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(command);

        ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
        verify(streamRepository).save(captor.capture());
        Stream saved = captor.getValue();
        assertThat(saved.getName().value()).isEqualTo("Events");
        assertThat(saved.getStreamType()).isEqualTo(StreamType.LOG);
        assertThat(saved.getStatus()).isEqualTo(StreamStatus.DRAFT);
    }

    @Test
    void should_ThrowStreamAlreadyExistsException_When_NameAlreadyExists() {
        CreateStreamCommand command = new CreateStreamCommand("Duplicate", null, ownerId, "EVENT");
        doThrow(new StreamAlreadyExistsException(StreamName.of("Duplicate")))
                .when(streamDomainService).validateStreamNameUnique(any(StreamName.class));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(StreamAlreadyExistsException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void should_ThrowIllegalArgumentException_When_InvalidStreamTypeProvided() {
        CreateStreamCommand command = new CreateStreamCommand("Stream", null, ownerId, "INVALID_TYPE");

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_ThrowNullPointerException_When_CommandIsNull() {
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(NullPointerException.class);
    }

}
