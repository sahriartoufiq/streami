package com.datastream.interfaces.grpc;

import com.datastream.application.dto.DataEventResponse;
import com.datastream.application.dto.PagedResponse;
import com.datastream.application.dto.StreamResponse;
import com.datastream.interfaces.grpc.proto.DataEvent;
import com.datastream.interfaces.grpc.proto.Pagination;
import com.datastream.interfaces.grpc.proto.Stream;
import com.datastream.interfaces.grpc.proto.StreamStatus;
import com.datastream.interfaces.grpc.proto.StreamType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link StreamProtoMapper}.
 */
class StreamProtoMapperTest {

    private static final String NOW_ISO = Instant.now().toString();
    private static final String STREAM_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    private static final String OWNER_ID  = "11111111-2222-3333-4444-555555555555";

    // -------------------------------------------------------------------------
    // toProtoStream
    // -------------------------------------------------------------------------

    @Test
    void should_MapAllFields_When_StreamResponseMapped() {
        StreamResponse response = new StreamResponse(
                STREAM_ID, "my-stream", "desc", OWNER_ID,
                "EVENT", "ACTIVE", NOW_ISO, NOW_ISO);

        Stream proto = StreamProtoMapper.toProtoStream(response);

        assertThat(proto.getId()).isEqualTo(STREAM_ID);
        assertThat(proto.getName()).isEqualTo("my-stream");
        assertThat(proto.getDescription()).isEqualTo("desc");
        assertThat(proto.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(proto.getStreamType()).isEqualTo(StreamType.STREAM_TYPE_EVENT);
        assertThat(proto.getStatus()).isEqualTo(StreamStatus.STREAM_STATUS_ACTIVE);
        assertThat(proto.getCreatedAt().getSeconds()).isPositive();
        assertThat(proto.getUpdatedAt().getSeconds()).isPositive();
    }

    @Test
    void should_SetDescriptionEmpty_When_DescriptionIsNull() {
        StreamResponse response = new StreamResponse(
                STREAM_ID, "no-desc", null, OWNER_ID,
                "LOG", "DRAFT", NOW_ISO, NOW_ISO);

        Stream proto = StreamProtoMapper.toProtoStream(response);

        assertThat(proto.getDescription()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // toProtoDataEvent
    // -------------------------------------------------------------------------

    @Test
    void should_MapAllFields_When_DataEventResponseMapped() {
        byte[] payload = "hello".getBytes();
        Map<String, String> metadata = Map.of("key", "value");
        DataEventResponse response = new DataEventResponse(
                "event-uuid", STREAM_ID, payload, metadata, NOW_ISO);

        DataEvent proto = StreamProtoMapper.toProtoDataEvent(response);

        assertThat(proto.getEventId()).isEqualTo("event-uuid");
        assertThat(proto.getStreamId()).isEqualTo(STREAM_ID);
        assertThat(proto.getPayload().toByteArray()).isEqualTo(payload);
        assertThat(proto.getMetadataMap()).containsEntry("key", "value");
        assertThat(proto.getTimestamp().getSeconds()).isPositive();
    }

    // -------------------------------------------------------------------------
    // toProtoPagination
    // -------------------------------------------------------------------------

    @Test
    void should_MapPaginationFields_When_PagedResponseMapped() {
        PagedResponse<StreamResponse> paged = new PagedResponse<>(List.of(), 2, 10, 25L, 3);

        Pagination pagination = StreamProtoMapper.toProtoPagination(paged);

        assertThat(pagination.getPage()).isEqualTo(2);
        assertThat(pagination.getSize()).isEqualTo(10);
        assertThat(pagination.getTotalElements()).isEqualTo(25L);
        assertThat(pagination.getTotalPages()).isEqualTo(3);
    }

    // -------------------------------------------------------------------------
    // toDomainStreamTypeName
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "STREAM_TYPE_EVENT,  EVENT",
            "STREAM_TYPE_LOG,    LOG",
            "STREAM_TYPE_METRIC, METRIC",
            "STREAM_TYPE_CUSTOM, CUSTOM"
    })
    void should_ReturnDomainName_When_ValidProtoStreamTypeMapped(
            StreamType protoType, String expectedDomainName) {
        assertThat(StreamProtoMapper.toDomainStreamTypeName(protoType))
                .isEqualTo(expectedDomainName);
    }

    @Test
    void should_ThrowIllegalArgumentException_When_UnspecifiedStreamTypeMapped() {
        assertThatThrownBy(() -> StreamProtoMapper.toDomainStreamTypeName(
                StreamType.STREAM_TYPE_UNSPECIFIED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNSPECIFIED");
    }

    // -------------------------------------------------------------------------
    // toDomainStreamStatusName
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "STREAM_STATUS_DRAFT,    DRAFT",
            "STREAM_STATUS_ACTIVE,   ACTIVE",
            "STREAM_STATUS_INACTIVE, INACTIVE",
            "STREAM_STATUS_DELETED,  DELETED"
    })
    void should_ReturnDomainName_When_ValidProtoStreamStatusMapped(
            StreamStatus protoStatus, String expectedDomainName) {
        assertThat(StreamProtoMapper.toDomainStreamStatusName(protoStatus))
                .isEqualTo(expectedDomainName);
    }

    @Test
    void should_ThrowIllegalArgumentException_When_UnspecifiedStreamStatusMapped() {
        assertThatThrownBy(() -> StreamProtoMapper.toDomainStreamStatusName(
                StreamStatus.STREAM_STATUS_UNSPECIFIED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNSPECIFIED");
    }

    // -------------------------------------------------------------------------
    // StreamType round-trip
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "STREAM_TYPE_EVENT",
            "STREAM_TYPE_LOG",
            "STREAM_TYPE_METRIC",
            "STREAM_TYPE_CUSTOM"
    })
    void should_PreserveStreamType_When_MappedThroughDomainAndBack(StreamType protoType) {
        String domainName = StreamProtoMapper.toDomainStreamTypeName(protoType);
        StreamResponse response = new StreamResponse(
                STREAM_ID, "s", null, OWNER_ID, domainName, "DRAFT", NOW_ISO, NOW_ISO);

        Stream proto = StreamProtoMapper.toProtoStream(response);

        assertThat(proto.getStreamType()).isEqualTo(protoType);
    }

    // -------------------------------------------------------------------------
    // StreamStatus round-trip
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "STREAM_STATUS_DRAFT",
            "STREAM_STATUS_ACTIVE",
            "STREAM_STATUS_INACTIVE",
            "STREAM_STATUS_DELETED"
    })
    void should_PreserveStreamStatus_When_MappedThroughDomainAndBack(StreamStatus protoStatus) {
        String domainName = StreamProtoMapper.toDomainStreamStatusName(protoStatus);
        StreamResponse response = new StreamResponse(
                STREAM_ID, "s", null, OWNER_ID, "EVENT", domainName, NOW_ISO, NOW_ISO);

        Stream proto = StreamProtoMapper.toProtoStream(response);

        assertThat(proto.getStatus()).isEqualTo(protoStatus);
    }
}
