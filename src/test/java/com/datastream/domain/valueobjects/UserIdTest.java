package com.datastream.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserIdTest {

    @Test
    void should_CreateUserId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();
        UserId userId = UserId.of(uuid);
        assertThat(userId.value()).isEqualTo(uuid);
    }

    @Test
    void should_ThrowNullPointerException_When_NullUuidProvided() {
        assertThatThrownBy(() -> UserId.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("UserId value must not be null");
    }

    @Test
    void should_BeEqual_When_SameUuidUsed() {
        UUID uuid = UUID.randomUUID();
        assertThat(UserId.of(uuid)).isEqualTo(UserId.of(uuid));
    }

    @Test
    void should_NotBeEqual_When_DifferentUuidsUsed() {
        assertThat(UserId.of(UUID.randomUUID())).isNotEqualTo(UserId.of(UUID.randomUUID()));
    }
}
