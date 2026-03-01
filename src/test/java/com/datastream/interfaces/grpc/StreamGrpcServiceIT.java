package com.datastream.interfaces.grpc;

import com.datastream.application.command.ActivateStreamCommand;
import com.datastream.application.command.CreateStreamCommand;
import com.datastream.application.command.DeactivateStreamCommand;
import com.datastream.application.command.DeleteStreamCommand;
import com.datastream.application.command.PublishEventCommand;
import com.datastream.application.command.UpdateStreamCommand;
import com.datastream.application.dto.DataEventResponse;
import com.datastream.application.dto.PagedResponse;
import com.datastream.application.dto.StreamResponse;
import com.datastream.application.handler.ActivateStreamHandler;
import com.datastream.application.handler.CreateStreamHandler;
import com.datastream.application.handler.DeactivateStreamHandler;
import com.datastream.application.handler.DeleteStreamHandler;
import com.datastream.application.handler.GetStreamHandler;
import com.datastream.application.handler.ListStreamsHandler;
import com.datastream.application.handler.PublishEventHandler;
import com.datastream.application.handler.UpdateStreamHandler;
import com.datastream.application.query.GetStreamQuery;
import com.datastream.application.query.ListStreamsQuery;
import com.datastream.domain.exception.StreamAlreadyExistsException;
import com.datastream.domain.exception.StreamNotFoundException;
import com.datastream.domain.valueobjects.StreamName;
import com.datastream.infrastructure.streaming.InMemoryStreamEventPublisher;
import com.datastream.interfaces.grpc.proto.CreateStreamRequest;
import com.datastream.interfaces.grpc.proto.CreateStreamResponse;
import com.datastream.interfaces.grpc.proto.DataEvent;
import com.datastream.interfaces.grpc.proto.DeleteStreamRequest;
import com.datastream.interfaces.grpc.proto.DeleteStreamResponse;
import com.datastream.interfaces.grpc.proto.GetStreamRequest;
import com.datastream.interfaces.grpc.proto.GetStreamResponse;
import com.datastream.interfaces.grpc.proto.ListStreamsRequest;
import com.datastream.interfaces.grpc.proto.ListStreamsResponse;
import com.datastream.interfaces.grpc.proto.PublishToStreamRequest;
import com.datastream.interfaces.grpc.proto.PublishToStreamResponse;
import com.datastream.interfaces.grpc.proto.StreamServiceGrpc;
import com.datastream.interfaces.grpc.proto.StreamStatus;
import com.datastream.interfaces.grpc.proto.StreamType;
import com.datastream.interfaces.grpc.proto.UpdateStreamRequest;
import com.datastream.interfaces.grpc.proto.UpdateStreamResponse;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link StreamGrpcService} using an in-process gRPC server.
 *
 * <p>All application-layer handlers are mocked so these tests focus on the
 * translation between protobuf messages and application commands/queries,
 * without requiring a database or full Spring context.
 */
@ExtendWith(MockitoExtension.class)
class StreamGrpcServiceIT {

    @RegisterExtension
    final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Mock CreateStreamHandler createStreamHandler;
    @Mock ActivateStreamHandler activateStreamHandler;
    @Mock DeactivateStreamHandler deactivateStreamHandler;
    @Mock UpdateStreamHandler updateStreamHandler;
    @Mock DeleteStreamHandler deleteStreamHandler;
    @Mock PublishEventHandler publishEventHandler;
    @Mock GetStreamHandler getStreamHandler;
    @Mock ListStreamsHandler listStreamsHandler;

    private StreamServiceGrpc.StreamServiceBlockingStub blockingStub;
    private StreamServiceGrpc.StreamServiceStub asyncStub;

    private final String streamId = UUID.randomUUID().toString();
    private final String ownerId  = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() throws IOException {
        InMemoryStreamEventPublisher eventPublisher = new InMemoryStreamEventPublisher();

        StreamGrpcService service = new StreamGrpcService(
                createStreamHandler, activateStreamHandler, deactivateStreamHandler,
                updateStreamHandler, deleteStreamHandler, publishEventHandler,
                getStreamHandler, listStreamsHandler, eventPublisher);

        String serverName = InProcessServerBuilder.generateName();
        grpcCleanup.register(
                InProcessServerBuilder.forName(serverName)
                        .directExecutor()
                        .addService(service)
                        .build()
                        .start());

        ManagedChannel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        blockingStub = StreamServiceGrpc.newBlockingStub(channel);
        asyncStub    = StreamServiceGrpc.newStub(channel);
    }

    // -------------------------------------------------------------------------
    // CreateStream
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnCreatedStream_When_CreateStreamCalled() {
        when(createStreamHandler.handle(any(CreateStreamCommand.class)))
                .thenReturn(streamResponseFixture("my-stream", "DRAFT"));

        CreateStreamResponse response = blockingStub.createStream(
                CreateStreamRequest.newBuilder()
                        .setName("my-stream")
                        .setOwnerId(ownerId)
                        .setStreamType(StreamType.STREAM_TYPE_EVENT)
                        .build());

        assertThat(response.getStream().getName()).isEqualTo("my-stream");
        assertThat(response.getStream().getStatus()).isEqualTo(StreamStatus.STREAM_STATUS_DRAFT);
    }

    @Test
    void should_PassCorrectCommandToHandler_When_CreateStreamCalled() {
        when(createStreamHandler.handle(any(CreateStreamCommand.class)))
                .thenReturn(streamResponseFixture("events", "DRAFT"));

        blockingStub.createStream(
                CreateStreamRequest.newBuilder()
                        .setName("events")
                        .setDescription("my desc")
                        .setOwnerId(ownerId)
                        .setStreamType(StreamType.STREAM_TYPE_LOG)
                        .build());

        var captor = org.mockito.ArgumentCaptor.forClass(CreateStreamCommand.class);
        verify(createStreamHandler).handle(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("events");
        assertThat(captor.getValue().description()).isEqualTo("my desc");
        assertThat(captor.getValue().streamType()).isEqualTo("LOG");
    }

    @Test
    void should_ThrowAlreadyExists_When_StreamNameAlreadyExists() {
        when(createStreamHandler.handle(any(CreateStreamCommand.class)))
                .thenThrow(new StreamAlreadyExistsException(StreamName.of("dup")));

        assertThatThrownBy(() -> blockingStub.createStream(
                CreateStreamRequest.newBuilder()
                        .setName("dup")
                        .setOwnerId(ownerId)
                        .setStreamType(StreamType.STREAM_TYPE_EVENT)
                        .build()))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(e -> assertThat(((StatusRuntimeException) e).getStatus().getCode())
                        .isEqualTo(Status.ALREADY_EXISTS.getCode()));
    }

    // -------------------------------------------------------------------------
    // GetStream
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnStream_When_GetStreamCalledWithExistingId() {
        when(getStreamHandler.handle(any(GetStreamQuery.class)))
                .thenReturn(streamResponseFixture("found", "ACTIVE"));

        GetStreamResponse response = blockingStub.getStream(
                GetStreamRequest.newBuilder().setId(streamId).build());

        assertThat(response.getStream().getName()).isEqualTo("found");
        assertThat(response.getStream().getStatus()).isEqualTo(StreamStatus.STREAM_STATUS_ACTIVE);
    }

    @Test
    void should_ThrowNotFound_When_GetStreamCalledWithNonExistentId() {
        when(getStreamHandler.handle(any(GetStreamQuery.class)))
                .thenThrow(new StreamNotFoundException("not found"));

        assertThatThrownBy(() -> blockingStub.getStream(
                GetStreamRequest.newBuilder().setId(streamId).build()))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(e -> assertThat(((StatusRuntimeException) e).getStatus().getCode())
                        .isEqualTo(Status.NOT_FOUND.getCode()));
    }

    // -------------------------------------------------------------------------
    // ListStreams
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnPagedStreams_When_ListStreamsCalled() {
        PagedResponse<StreamResponse> paged = new PagedResponse<>(
                List.of(streamResponseFixture("s1", "ACTIVE"),
                        streamResponseFixture("s2", "DRAFT")),
                0, 20, 2L, 1);
        when(listStreamsHandler.handle(any(ListStreamsQuery.class))).thenReturn(paged);

        ListStreamsResponse response = blockingStub.listStreams(
                ListStreamsRequest.newBuilder().setPage(0).setSize(20).build());

        assertThat(response.getStreamsList()).hasSize(2);
        assertThat(response.getPagination().getTotalElements()).isEqualTo(2L);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // UpdateStream
    // -------------------------------------------------------------------------

    @Test
    void should_ActivateStream_When_UpdateStreamCalledWithActiveStatus() {
        when(getStreamHandler.handle(any(GetStreamQuery.class)))
                .thenReturn(streamResponseFixture("s", "ACTIVE"));

        UpdateStreamResponse response = blockingStub.updateStream(
                UpdateStreamRequest.newBuilder()
                        .setId(streamId)
                        .setStatus(StreamStatus.STREAM_STATUS_ACTIVE)
                        .build());

        verify(activateStreamHandler).handle(any(ActivateStreamCommand.class));
        assertThat(response.getStream().getStatus()).isEqualTo(StreamStatus.STREAM_STATUS_ACTIVE);
    }

    @Test
    void should_DeactivateStream_When_UpdateStreamCalledWithInactiveStatus() {
        when(getStreamHandler.handle(any(GetStreamQuery.class)))
                .thenReturn(streamResponseFixture("s", "INACTIVE"));

        blockingStub.updateStream(
                UpdateStreamRequest.newBuilder()
                        .setId(streamId)
                        .setStatus(StreamStatus.STREAM_STATUS_INACTIVE)
                        .build());

        verify(deactivateStreamHandler).handle(any(DeactivateStreamCommand.class));
    }

    @Test
    void should_UpdateConfig_When_UpdateStreamCalledWithName() {
        when(updateStreamHandler.handle(any(UpdateStreamCommand.class)))
                .thenReturn(streamResponseFixture("renamed", "ACTIVE"));
        when(getStreamHandler.handle(any(GetStreamQuery.class)))
                .thenReturn(streamResponseFixture("renamed", "ACTIVE"));

        UpdateStreamResponse response = blockingStub.updateStream(
                UpdateStreamRequest.newBuilder()
                        .setId(streamId)
                        .setName("renamed")
                        .build());

        verify(updateStreamHandler).handle(any(UpdateStreamCommand.class));
        assertThat(response.getStream().getName()).isEqualTo("renamed");
    }

    // -------------------------------------------------------------------------
    // DeleteStream
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnSuccess_When_DeleteStreamCalled() {
        DeleteStreamResponse response = blockingStub.deleteStream(
                DeleteStreamRequest.newBuilder().setId(streamId).build());

        verify(deleteStreamHandler).handle(any(DeleteStreamCommand.class));
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isNotBlank();
    }

    @Test
    void should_ThrowNotFound_When_DeleteStreamCalledWithNonExistentId() {
        org.mockito.Mockito.doThrow(new StreamNotFoundException("not found"))
                .when(deleteStreamHandler).handle(any(DeleteStreamCommand.class));

        assertThatThrownBy(() -> blockingStub.deleteStream(
                DeleteStreamRequest.newBuilder().setId(streamId).build()))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(e -> assertThat(((StatusRuntimeException) e).getStatus().getCode())
                        .isEqualTo(Status.NOT_FOUND.getCode()));
    }

    // -------------------------------------------------------------------------
    // PublishToStream (client-streaming)
    // -------------------------------------------------------------------------

    @Test
    void should_ReturnAcceptedCount_When_PublishToStreamCalled() throws InterruptedException {
        DataEventResponse savedEvent = new DataEventResponse(
                UUID.randomUUID().toString(), streamId, "data".getBytes(),
                Collections.emptyMap(), Instant.now().toString());
        when(publishEventHandler.handle(any(PublishEventCommand.class))).thenReturn(savedEvent);

        var latch = new java.util.concurrent.CountDownLatch(1);
        var responseHolder = new java.util.concurrent.atomic.AtomicReference<PublishToStreamResponse>();

        io.grpc.stub.StreamObserver<PublishToStreamRequest> requestObserver =
                asyncStub.publishToStream(new io.grpc.stub.StreamObserver<>() {
                    @Override public void onNext(PublishToStreamResponse r) { responseHolder.set(r); }
                    @Override public void onError(Throwable t) { latch.countDown(); }
                    @Override public void onCompleted() { latch.countDown(); }
                });

        requestObserver.onNext(PublishToStreamRequest.newBuilder()
                .setStreamId(streamId)
                .setPayload(ByteString.copyFrom("event1".getBytes()))
                .build());
        requestObserver.onNext(PublishToStreamRequest.newBuilder()
                .setStreamId(streamId)
                .setPayload(ByteString.copyFrom("event2".getBytes()))
                .build());
        requestObserver.onCompleted();

        assertThat(latch.await(2, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        assertThat(responseHolder.get().getEventsAccepted()).isEqualTo(2L);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private StreamResponse streamResponseFixture(String name, String status) {
        String now = Instant.now().toString();
        return new StreamResponse(streamId, name, null, ownerId, "EVENT", status, now, now);
    }
}
