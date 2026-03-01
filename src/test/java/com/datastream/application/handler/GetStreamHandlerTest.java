package com.datastream.application.handler;

import com.datastream.application.dto.StreamResponse;
import com.datastream.application.query.GetStreamQuery;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStreamHandlerTest {

    @Mock StreamRepository streamRepository;

    private GetStreamHandler handler;
    private StreamId streamId;
    private Stream activeStream;

    @BeforeEach
    void setUp() {
        handler = new GetStreamHandler(streamRepository);
        streamId = StreamId.generate();
        activeStream = Stream.reconstitute(streamId, StreamName.of("My Stream"), "desc",
                UserId.of(UUID.randomUUID()), StreamType.METRIC, StreamStatus.ACTIVE,
                Instant.now(), Instant.now());
    }

    @Test
    void should_ReturnStreamResponse_When_StreamExists() {
        when(streamRepository.findById(streamId)).thenReturn(Optional.of(activeStream));

        StreamResponse response = handler.handle(new GetStreamQuery(streamId.value().toString()));

        assertThat(response.id()).isEqualTo(streamId.value().toString());
        assertThat(response.name()).isEqualTo("My Stream");
        assertThat(response.description()).isEqualTo("desc");
        assertThat(response.streamType()).isEqualTo("METRIC");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void should_ThrowStreamNotFoundException_When_StreamDoesNotExist() {
        when(streamRepository.findById(streamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetStreamQuery(streamId.value().toString())))
                .isInstanceOf(StreamNotFoundException.class)
                .hasMessageContaining(streamId.value().toString());
    }

    @Test
    void should_ThrowNullPointerException_When_QueryIsNull() {
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(NullPointerException.class);
    }
}
