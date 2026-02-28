package com.datastream.application.handler;

import com.datastream.application.command.PublishEventCommand;
import com.datastream.application.dto.DataEventResponse;
import com.datastream.application.port.StreamEventPublisher;
import com.datastream.domain.exception.InvalidStreamStateException;
import com.datastream.domain.exception.StreamNotFoundException;
import com.datastream.domain.model.DataEvent;
import com.datastream.domain.repository.DataEventRepository;
import com.datastream.domain.service.StreamDomainService;
import com.datastream.domain.valueobjects.StreamId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishEventHandlerTest {

    @Mock StreamDomainService streamDomainService;
    @Mock DataEventRepository dataEventRepository;
    @Mock StreamEventPublisher eventPublisher;

    private PublishEventHandler handler;
    private PublishEventHandler handlerWithoutPublisher;
    private String streamIdStr;

    @BeforeEach
    void setUp() {
        handler = new PublishEventHandler(streamDomainService, dataEventRepository, eventPublisher);
        handlerWithoutPublisher = new PublishEventHandler(streamDomainService, dataEventRepository, null);
        streamIdStr = UUID.randomUUID().toString();
    }

    @Test
    void should_ReturnDataEventResponse_When_StreamIsActive() {
        when(dataEventRepository.save(any(DataEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        byte[] payload = "data".getBytes();
        PublishEventCommand command = new PublishEventCommand(streamIdStr, payload, Collections.emptyMap());

        DataEventResponse response = handler.handle(command);

        assertThat(response.streamId()).isEqualTo(streamIdStr);
        assertThat(response.payload()).isEqualTo(payload);
        assertThat(response.eventId()).isNotNull();
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void should_PersistEvent_When_StreamIsActive() {
        when(dataEventRepository.save(any(DataEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        PublishEventCommand command = new PublishEventCommand(streamIdStr, "data".getBytes(), Map.of("k", "v"));

        handler.handle(command);

        ArgumentCaptor<DataEvent> captor = ArgumentCaptor.forClass(DataEvent.class);
        verify(dataEventRepository).save(captor.capture());
        assertThat(captor.getValue().getMetadata()).containsEntry("k", "v");
    }

    @Test
    void should_NotifyPublisher_When_PublisherIsPresent() {
        when(dataEventRepository.save(any(DataEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        PublishEventCommand command = new PublishEventCommand(streamIdStr, "data".getBytes(), Collections.emptyMap());

        handler.handle(command);

        verify(eventPublisher).publish(any(DataEventResponse.class));
    }

    @Test
    void should_NotThrow_When_PublisherIsAbsent() {
        when(dataEventRepository.save(any(DataEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        PublishEventCommand command = new PublishEventCommand(streamIdStr, "data".getBytes(), Collections.emptyMap());

        DataEventResponse response = handlerWithoutPublisher.handle(command);

        assertThat(response).isNotNull();
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void should_ThrowInvalidStreamStateException_When_StreamIsInactive() {
        doThrow(new InvalidStreamStateException("Stream is not ACTIVE"))
                .when(streamDomainService).validateStreamIsActive(any(StreamId.class));
        PublishEventCommand command = new PublishEventCommand(streamIdStr, "data".getBytes(), Collections.emptyMap());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InvalidStreamStateException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test
    void should_ThrowStreamNotFoundException_When_StreamDoesNotExist() {
        doThrow(new StreamNotFoundException("Stream not found"))
                .when(streamDomainService).validateStreamIsActive(any(StreamId.class));
        PublishEventCommand command = new PublishEventCommand(streamIdStr, "data".getBytes(), Collections.emptyMap());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(StreamNotFoundException.class);
    }

    @Test
    void should_ThrowNullPointerException_When_CommandIsNull() {
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(NullPointerException.class);
    }
}
