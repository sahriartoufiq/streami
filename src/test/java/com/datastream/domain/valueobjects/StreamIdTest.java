package com.datastream.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreamIdTest {

    @Test
    void should_CreateStreamId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();
        StreamId streamId = StreamId.of(uuid);
        assertThat(streamId.value()).isEqualTo(uuid);
    }

    @Test
    void should_ThrowNullPointerException_When_NullUuidProvided() {
        assertThatThrownBy(() -> StreamId.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("StreamId value must not be null");
    }

    @Test
    void should_GenerateUniqueStreamId_When_GenerateCalled() {
        StreamId first = StreamId.generate();
        StreamId second = StreamId.generate();
        assertThat(first).isNotEqualTo(second);
        assertThat(first.value()).isNotNull();
        assertThat(second.value()).isNotNull();
    }

    @Test
    void should_BeEqual_When_SameUuidUsed() {
        UUID uuid = UUID.randomUUID();
        assertThat(StreamId.of(uuid)).isEqualTo(StreamId.of(uuid));
    }

    @Test
    void should_NotBeEqual_When_DifferentUuidsUsed() {
        assertThat(StreamId.generate()).isNotEqualTo(StreamId.generate());
    }
}
