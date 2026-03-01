package com.datastream.interfaces.grpc;

import com.datastream.application.dto.DataEventResponse;
import com.datastream.application.dto.PagedResponse;
import com.datastream.application.dto.StreamResponse;
import com.datastream.interfaces.grpc.proto.DataEvent;
import com.datastream.interfaces.grpc.proto.Pagination;
import com.datastream.interfaces.grpc.proto.Stream;
import com.datastream.interfaces.grpc.proto.StreamStatus;
import com.datastream.interfaces.grpc.proto.StreamType;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import java.time.Instant;

/**
 * Utility class that maps between protobuf messages and application-layer DTOs.
 *
 * <p>All methods are static; this class is not intended to be instantiated.
 */
public final class StreamProtoMapper {

    private StreamProtoMapper() {
    }

    // -------------------------------------------------------------------------
    // DTO → Proto
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link StreamResponse} DTO to a {@link Stream} proto message.
     *
     * @param response the DTO to map; must not be null
     * @return the corresponding proto message
     */
    public static Stream toProtoStream(StreamResponse response) {
        Stream.Builder builder = Stream.newBuilder()
                .setId(response.id())
                .setName(response.name())
                .setOwnerId(response.ownerId())
                .setStreamType(toProtoStreamType(response.streamType()))
                .setStatus(toProtoStreamStatus(response.status()))
                .setCreatedAt(toProtoTimestamp(response.createdAt()))
                .setUpdatedAt(toProtoTimestamp(response.updatedAt()));

        if (response.description() != null) {
            builder.setDescription(response.description());
        }

        return builder.build();
    }

    /**
     * Maps a {@link DataEventResponse} DTO to a {@link DataEvent} proto message.
     *
     * @param response the DTO to map; must not be null
     * @return the corresponding proto message
     */
    public static DataEvent toProtoDataEvent(DataEventResponse response) {
        return DataEvent.newBuilder()
                .setEventId(response.eventId())
                .setStreamId(response.streamId())
                .setPayload(ByteString.copyFrom(response.payload()))
                .putAllMetadata(response.metadata())
                .setTimestamp(toProtoTimestamp(response.timestamp()))
                .build();
    }

    /**
     * Maps a {@link PagedResponse} to a {@link Pagination} proto message.
     *
     * @param paged the paged response whose metadata to extract; must not be null
     * @return the corresponding proto pagination message
     */
    public static Pagination toProtoPagination(PagedResponse<?> paged) {
        return Pagination.newBuilder()
                .setPage(paged.page())
                .setSize(paged.size())
                .setTotalElements(paged.totalElements())
                .setTotalPages(paged.totalPages())
                .build();
    }

    // -------------------------------------------------------------------------
    // Proto enum → domain string
    // -------------------------------------------------------------------------

    /**
     * Converts a proto {@link StreamType} enum value to the domain string name
     * expected by the application layer (e.g. {@code "EVENT"}).
     *
     * @param protoType the proto enum value; must not be {@code STREAM_TYPE_UNSPECIFIED}
     * @return the domain string representation
     * @throws IllegalArgumentException if {@code protoType} is unspecified or unknown
     */
    public static String toDomainStreamTypeName(StreamType protoType) {
        return switch (protoType) {
            case STREAM_TYPE_EVENT   -> "EVENT";
            case STREAM_TYPE_LOG     -> "LOG";
            case STREAM_TYPE_METRIC  -> "METRIC";
            case STREAM_TYPE_CUSTOM  -> "CUSTOM";
            default -> throw new IllegalArgumentException("Unsupported stream type: " + protoType);
        };
    }

    /**
     * Converts a proto {@link StreamStatus} enum value to the domain string name
     * expected by the application layer (e.g. {@code "ACTIVE"}).
     *
     * @param protoStatus the proto enum value; must not be {@code STREAM_STATUS_UNSPECIFIED}
     * @return the domain string representation
     * @throws IllegalArgumentException if {@code protoStatus} is unspecified or unknown
     */
    public static String toDomainStreamStatusName(StreamStatus protoStatus) {
        return switch (protoStatus) {
            case STREAM_STATUS_DRAFT     -> "DRAFT";
            case STREAM_STATUS_ACTIVE    -> "ACTIVE";
            case STREAM_STATUS_INACTIVE  -> "INACTIVE";
            case STREAM_STATUS_DELETED   -> "DELETED";
            default -> throw new IllegalArgumentException("Unsupported stream status: " + protoStatus);
        };
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static StreamType toProtoStreamType(String domainStreamType) {
        return switch (domainStreamType) {
            case "EVENT"  -> StreamType.STREAM_TYPE_EVENT;
            case "LOG"    -> StreamType.STREAM_TYPE_LOG;
            case "METRIC" -> StreamType.STREAM_TYPE_METRIC;
            case "CUSTOM" -> StreamType.STREAM_TYPE_CUSTOM;
            default -> StreamType.STREAM_TYPE_UNSPECIFIED;
        };
    }

    private static StreamStatus toProtoStreamStatus(String domainStatus) {
        return switch (domainStatus) {
            case "DRAFT"    -> StreamStatus.STREAM_STATUS_DRAFT;
            case "ACTIVE"   -> StreamStatus.STREAM_STATUS_ACTIVE;
            case "INACTIVE" -> StreamStatus.STREAM_STATUS_INACTIVE;
            case "DELETED"  -> StreamStatus.STREAM_STATUS_DELETED;
            default -> StreamStatus.STREAM_STATUS_UNSPECIFIED;
        };
    }

    private static Timestamp toProtoTimestamp(String isoInstant) {
        Instant instant = Instant.parse(isoInstant);
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
