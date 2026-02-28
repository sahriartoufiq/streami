package com.datastream.application.handler;

import com.datastream.application.dto.PagedResponse;
import com.datastream.application.dto.StreamResponse;
import com.datastream.application.query.ListStreamsQuery;
import com.datastream.domain.model.Page;
import com.datastream.domain.model.Stream;
import com.datastream.domain.model.StreamFilter;
import com.datastream.domain.model.StreamStatus;
import com.datastream.domain.model.StreamType;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.valueobjects.StreamId;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.domain.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListStreamsHandlerTest {

    @Mock StreamRepository streamRepository;

    private ListStreamsHandler handler;
    private final String ownerId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        handler = new ListStreamsHandler(streamRepository);
    }

    @Test
    void should_ReturnPagedResponse_When_StreamsExist() {
        Stream stream = buildStream(StreamStatus.ACTIVE);
        Page<Stream> page = new Page<>(List.of(stream), 0, 10, 1L);
        when(streamRepository.findAll(any(StreamFilter.class), anyInt(), anyInt())).thenReturn(page);

        PagedResponse<StreamResponse> response = handler.handle(new ListStreamsQuery(null, null, null, 0, 10));

        assertThat(response.content()).hasSize(1);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(1L);
        assertThat(response.totalPages()).isEqualTo(1);
    }

    @Test
    void should_ReturnEmptyPagedResponse_When_NoStreamsMatch() {
        Page<Stream> emptyPage = new Page<>(Collections.emptyList(), 0, 10, 0L);
        when(streamRepository.findAll(any(StreamFilter.class), anyInt(), anyInt())).thenReturn(emptyPage);

        PagedResponse<StreamResponse> response = handler.handle(new ListStreamsQuery(null, null, null, 0, 10));

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0L);
        assertThat(response.totalPages()).isEqualTo(0);
    }

    @Test
    void should_PassFiltersToRepository_When_AllFiltersProvided() {
        Page<Stream> page = new Page<>(Collections.emptyList(), 0, 5, 0L);
        when(streamRepository.findAll(any(StreamFilter.class), anyInt(), anyInt())).thenReturn(page);

        handler.handle(new ListStreamsQuery(ownerId, "ACTIVE", "EVENT", 0, 5));

        ArgumentCaptor<StreamFilter> filterCaptor = ArgumentCaptor.forClass(StreamFilter.class);
        verify(streamRepository).findAll(filterCaptor.capture(), anyInt(), anyInt());

        StreamFilter captured = filterCaptor.getValue();
        assertThat(captured.ownerId().value().toString()).isEqualTo(ownerId);
        assertThat(captured.status()).isEqualTo(StreamStatus.ACTIVE);
        assertThat(captured.streamType()).isEqualTo(StreamType.EVENT);
    }

    @Test
    void should_PassNullFiltersToRepository_When_NoFiltersProvided() {
        Page<Stream> page = new Page<>(Collections.emptyList(), 0, 10, 0L);
        when(streamRepository.findAll(any(StreamFilter.class), anyInt(), anyInt())).thenReturn(page);

        handler.handle(new ListStreamsQuery(null, null, null, 0, 10));

        ArgumentCaptor<StreamFilter> filterCaptor = ArgumentCaptor.forClass(StreamFilter.class);
        verify(streamRepository).findAll(filterCaptor.capture(), anyInt(), anyInt());

        StreamFilter captured = filterCaptor.getValue();
        assertThat(captured.ownerId()).isNull();
        assertThat(captured.status()).isNull();
        assertThat(captured.streamType()).isNull();
    }

    @Test
    void should_HandlePagination_When_RequestingSecondPage() {
        Stream s1 = buildStream(StreamStatus.ACTIVE);
        Stream s2 = buildStream(StreamStatus.DRAFT);
        Page<Stream> page = new Page<>(List.of(s1, s2), 1, 2, 5L);
        when(streamRepository.findAll(any(StreamFilter.class), anyInt(), anyInt())).thenReturn(page);

        PagedResponse<StreamResponse> response = handler.handle(new ListStreamsQuery(null, null, null, 1, 2));

        assertThat(response.content()).hasSize(2);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5L);
        assertThat(response.totalPages()).isEqualTo(3);
    }

    @Test
    void should_MapAllStreamFieldsCorrectly_When_StreamsReturned() {
        Stream stream = buildStream(StreamStatus.ACTIVE);
        Page<Stream> page = new Page<>(List.of(stream), 0, 10, 1L);
        when(streamRepository.findAll(any(StreamFilter.class), anyInt(), anyInt())).thenReturn(page);

        PagedResponse<StreamResponse> response = handler.handle(new ListStreamsQuery(null, null, null, 0, 10));

        StreamResponse sr = response.content().get(0);
        assertThat(sr.name()).isEqualTo("Test Stream");
        assertThat(sr.status()).isEqualTo("ACTIVE");
        assertThat(sr.streamType()).isEqualTo("EVENT");
    }

    @Test
    void should_ThrowNullPointerException_When_QueryIsNull() {
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(NullPointerException.class);
    }

    private Stream buildStream(StreamStatus status) {
        return Stream.reconstitute(
                StreamId.generate(), StreamName.of("Test Stream"), null,
                UserId.of(UUID.fromString(ownerId)), StreamType.EVENT, status,
                Instant.now(), Instant.now());
    }
}
