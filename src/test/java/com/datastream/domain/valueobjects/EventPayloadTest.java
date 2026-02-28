package com.datastream.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventPayloadTest {

    @Test
    void should_CreateEventPayload_When_ValidBytesProvided() {
        byte[] bytes = "hello".getBytes();
        EventPayload payload = EventPayload.of(bytes);
        assertThat(payload.value()).isEqualTo(bytes);
    }

    @Test
    void should_CreateEventPayload_When_EmptyBytesProvided() {
        EventPayload payload = EventPayload.of(new byte[0]);
        assertThat(payload.value()).isEmpty();
    }

    @Test
    void should_ThrowNullPointerException_When_NullBytesProvided() {
        assertThatThrownBy(() -> EventPayload.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("EventPayload must not be null");
    }

    @Test
    void should_ThrowIllegalArgumentException_When_PayloadExceeds1MB() {
        byte[] tooBig = new byte[EventPayload.MAX_SIZE_BYTES + 1];
        assertThatThrownBy(() -> EventPayload.of(tooBig))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 MB");
    }

    @Test
    void should_CreateEventPayload_When_PayloadIsExactly1MB() {
        byte[] exactly1MB = new byte[EventPayload.MAX_SIZE_BYTES];
        EventPayload payload = EventPayload.of(exactly1MB);
        assertThat(payload.value()).hasSize(EventPayload.MAX_SIZE_BYTES);
    }

    @Test
    void should_ReturnDefensiveCopy_When_ValueRetrieved() {
        byte[] original = {1, 2, 3};
        EventPayload payload = EventPayload.of(original);
        byte[] retrieved = payload.value();
        retrieved[0] = 99;
        assertThat(payload.value()[0]).isEqualTo((byte) 1);
    }

    @Test
    void should_NotMutateInternalState_When_OriginalArrayModified() {
        byte[] original = {1, 2, 3};
        EventPayload payload = EventPayload.of(original);
        original[0] = 99;
        assertThat(payload.value()[0]).isEqualTo((byte) 1);
    }

    @Test
    void should_BeEqual_When_SameBytesProvided() {
        byte[] bytes = {1, 2, 3};
        assertThat(EventPayload.of(bytes)).isEqualTo(EventPayload.of(bytes));
    }

    @Test
    void should_NotBeEqual_When_DifferentBytesProvided() {
        assertThat(EventPayload.of(new byte[]{1})).isNotEqualTo(EventPayload.of(new byte[]{2}));
    }

    @Test
    void should_IncludeSizeInToString_When_ToStringCalled() {
        EventPayload payload = EventPayload.of(new byte[42]);
        assertThat(payload.toString()).contains("42");
    }
}
