package com.datastream.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreamNameTest {

    @Test
    void should_CreateStreamName_When_ValidStringProvided() {
        StreamName name = StreamName.of("My Stream");
        assertThat(name.value()).isEqualTo("My Stream");
    }

    @Test
    void should_TrimWhitespace_When_LeadingOrTrailingSpacesPresent() {
        StreamName name = StreamName.of("  My Stream  ");
        assertThat(name.value()).isEqualTo("My Stream");
    }

    @Test
    void should_ThrowNullPointerException_When_NullValueProvided() {
        assertThatThrownBy(() -> StreamName.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("StreamName must not be null");
    }

    @Test
    void should_ThrowIllegalArgumentException_When_BlankStringProvided() {
        assertThatThrownBy(() -> StreamName.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StreamName must not be blank");
    }

    @Test
    void should_ThrowIllegalArgumentException_When_EmptyStringProvided() {
        assertThatThrownBy(() -> StreamName.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StreamName must not be blank");
    }

    @Test
    void should_ThrowIllegalArgumentException_When_NameExceeds255Characters() {
        String tooLong = "a".repeat(256);
        assertThatThrownBy(() -> StreamName.of(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255");
    }

    @Test
    void should_CreateStreamName_When_NameIsExactly255Characters() {
        String exactly255 = "a".repeat(255);
        StreamName name = StreamName.of(exactly255);
        assertThat(name.value()).hasSize(255);
    }

    @Test
    void should_ThrowIllegalArgumentException_When_NameBecomesTooLongAfterTrim() {
        // Trimmed value itself exceeds 255 chars
        String tooLong = "a".repeat(256);
        assertThatThrownBy(() -> StreamName.of(tooLong))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_BeEqual_When_SameValueUsed() {
        assertThat(StreamName.of("events")).isEqualTo(StreamName.of("events"));
    }

    @Test
    void should_NotBeEqual_When_DifferentValuesUsed() {
        assertThat(StreamName.of("events")).isNotEqualTo(StreamName.of("logs"));
    }
}
