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
import com.datastream.interfaces.grpc.proto.SubscribeToStreamRequest;
import com.datastream.interfaces.grpc.proto.UpdateStreamRequest;
import com.datastream.interfaces.grpc.proto.UpdateStreamResponse;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * gRPC service adapter for the {@code StreamService} proto definition.
 *
 * <p>This class is a thin adapter: it translates protobuf requests into
 * application-layer commands/queries, delegates to the appropriate handler,
 * and maps responses back to protobuf messages. No business logic lives here.
 */
@GrpcService
public class StreamGrpcService extends StreamServiceGrpc.StreamServiceImplBase {

    private final CreateStreamHandler createStreamHandler;
    private final ActivateStreamHandler activateStreamHandler;
    private final DeactivateStreamHandler deactivateStreamHandler;
    private final UpdateStreamHandler updateStreamHandler;
    private final DeleteStreamHandler deleteStreamHandler;
    private final PublishEventHandler publishEventHandler;
    private final GetStreamHandler getStreamHandler;
    private final ListStreamsHandler listStreamsHandler;
    private final InMemoryStreamEventPublisher eventPublisher;

    /**
     * Creates the service with all required handler and publisher dependencies.
     *
     * @param createStreamHandler     handler for creating streams
     * @param activateStreamHandler   handler for activating streams
     * @param deactivateStreamHandler handler for deactivating streams
     * @param updateStreamHandler     handler for updating stream config
     * @param deleteStreamHandler     handler for soft-deleting streams
     * @param publishEventHandler     handler for publishing data events
     * @param getStreamHandler        handler for fetching a single stream
     * @param listStreamsHandler      handler for listing streams
     * @param eventPublisher          in-memory publisher for server-streaming subscriptions
     */
    public StreamGrpcService(
            CreateStreamHandler createStreamHandler,
            ActivateStreamHandler activateStreamHandler,
            DeactivateStreamHandler deactivateStreamHandler,
            UpdateStreamHandler updateStreamHandler,
            DeleteStreamHandler deleteStreamHandler,
            PublishEventHandler publishEventHandler,
            GetStreamHandler getStreamHandler,
            ListStreamsHandler listStreamsHandler,
            InMemoryStreamEventPublisher eventPublisher) {
        this.createStreamHandler = Objects.requireNonNull(createStreamHandler);
        this.activateStreamHandler = Objects.requireNonNull(activateStreamHandler);
        this.deactivateStreamHandler = Objects.requireNonNull(deactivateStreamHandler);
        this.updateStreamHandler = Objects.requireNonNull(updateStreamHandler);
        this.deleteStreamHandler = Objects.requireNonNull(deleteStreamHandler);
        this.publishEventHandler = Objects.requireNonNull(publishEventHandler);
        this.getStreamHandler = Objects.requireNonNull(getStreamHandler);
        this.listStreamsHandler = Objects.requireNonNull(listStreamsHandler);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    // -------------------------------------------------------------------------
    // Unary RPCs
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void createStream(CreateStreamRequest request,
                             StreamObserver<CreateStreamResponse> responseObserver) {
        CreateStreamCommand command = new CreateStreamCommand(
                request.getName(),
                request.getDescription().isEmpty() ? null : request.getDescription(),
                request.getOwnerId(),
                StreamProtoMapper.toDomainStreamTypeName(request.getStreamType()));

        StreamResponse response = createStreamHandler.handle(command);

        responseObserver.onNext(CreateStreamResponse.newBuilder()
                .setStream(StreamProtoMapper.toProtoStream(response))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getStream(GetStreamRequest request,
                          StreamObserver<GetStreamResponse> responseObserver) {
        StreamResponse response = getStreamHandler.handle(new GetStreamQuery(request.getId()));

        responseObserver.onNext(GetStreamResponse.newBuilder()
                .setStream(StreamProtoMapper.toProtoStream(response))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void listStreams(ListStreamsRequest request,
                            StreamObserver<ListStreamsResponse> responseObserver) {
        String ownerId = null;
        String status = null;
        String streamType = null;

        if (request.hasFilter()) {
            var filter = request.getFilter();
            if (filter.hasOwnerId()) {
                ownerId = filter.getOwnerId();
            }
            if (filter.hasStatus() && filter.getStatus() != StreamStatus.STREAM_STATUS_UNSPECIFIED) {
                status = StreamProtoMapper.toDomainStreamStatusName(filter.getStatus());
            }
            if (filter.hasStreamType()) {
                streamType = StreamProtoMapper.toDomainStreamTypeName(filter.getStreamType());
            }
        }

        ListStreamsQuery query = new ListStreamsQuery(ownerId, status, streamType,
                request.getPage(), request.getSize() > 0 ? request.getSize() : 20);

        PagedResponse<StreamResponse> paged = listStreamsHandler.handle(query);

        ListStreamsResponse.Builder builder = ListStreamsResponse.newBuilder()
                .setPagination(StreamProtoMapper.toProtoPagination(paged));
        paged.content().forEach(s -> builder.addStreams(StreamProtoMapper.toProtoStream(s)));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Applies partial updates: if {@code status} is set, a lifecycle transition
     * is performed first; if {@code name} is set, the config is updated. Both
     * may be applied in a single call.
     */
    @Override
    public void updateStream(UpdateStreamRequest request,
                             StreamObserver<UpdateStreamResponse> responseObserver) {
        String streamId = request.getId();

        if (request.hasStatus()) {
            switch (request.getStatus()) {
                case STREAM_STATUS_ACTIVE ->
                        activateStreamHandler.handle(new ActivateStreamCommand(streamId));
                case STREAM_STATUS_INACTIVE ->
                        deactivateStreamHandler.handle(new DeactivateStreamCommand(streamId));
                case STREAM_STATUS_DELETED ->
                        deleteStreamHandler.handle(new DeleteStreamCommand(streamId));
                default -> throw new IllegalArgumentException(
                        "Cannot transition to status: " + request.getStatus());
            }
        }

        if (request.hasName()) {
            String description = request.hasDescription() ? request.getDescription() : null;
            updateStreamHandler.handle(new UpdateStreamCommand(streamId, request.getName(), description));
        }

        StreamResponse updated = getStreamHandler.handle(new GetStreamQuery(streamId));
        responseObserver.onNext(UpdateStreamResponse.newBuilder()
                .setStream(StreamProtoMapper.toProtoStream(updated))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteStream(DeleteStreamRequest request,
                             StreamObserver<DeleteStreamResponse> responseObserver) {
        deleteStreamHandler.handle(new DeleteStreamCommand(request.getId()));

        responseObserver.onNext(DeleteStreamResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Stream deleted successfully")
                .build());
        responseObserver.onCompleted();
    }

    // -------------------------------------------------------------------------
    // Server-streaming RPC
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Subscribes the caller to live events published to the requested stream.
     * Events are delivered in publish order on a virtual thread. The subscription
     * remains active until the client cancels or the server calls
     * {@link StreamObserver#onCompleted()}.
     */
    @Override
    public void subscribeToStream(SubscribeToStreamRequest request,
                                  StreamObserver<DataEvent> responseObserver) {
        String streamId = request.getStreamId();

        // Validate stream exists (throws StreamNotFoundException if missing)
        getStreamHandler.handle(new GetStreamQuery(streamId));

        LinkedBlockingQueue<DataEventResponse> queue = new LinkedBlockingQueue<>(1000);
        eventPublisher.subscribe(streamId, queue);

        ServerCallStreamObserver<DataEvent> serverObserver =
                (ServerCallStreamObserver<DataEvent>) responseObserver;
        serverObserver.setOnCancelHandler(() -> eventPublisher.unsubscribe(streamId, queue));

        Thread.ofVirtual().name("subscribe-" + streamId).start(() -> {
            try {
                while (!serverObserver.isCancelled()) {
                    DataEventResponse event = queue.poll(500, TimeUnit.MILLISECONDS);
                    if (event != null && !serverObserver.isCancelled()) {
                        responseObserver.onNext(StreamProtoMapper.toProtoDataEvent(event));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                eventPublisher.unsubscribe(streamId, queue);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Client-streaming RPC
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Accepts a stream of publish requests from the client. Each request
     * is processed by the {@link PublishEventHandler}. When the client closes
     * the stream, the total accepted count is returned.
     */
    @Override
    public StreamObserver<PublishToStreamRequest> publishToStream(
            StreamObserver<PublishToStreamResponse> responseObserver) {

        AtomicLong acceptedCount = new AtomicLong(0);

        return new StreamObserver<>() {
            @Override
            public void onNext(PublishToStreamRequest request) {
                publishEventHandler.handle(new PublishEventCommand(
                        request.getStreamId(),
                        request.getPayload().toByteArray(),
                        request.getMetadataMap()));
                acceptedCount.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                // Client-side error; nothing to complete
            }

            @Override
            public void onCompleted() {
                long count = acceptedCount.get();
                responseObserver.onNext(PublishToStreamResponse.newBuilder()
                        .setEventsAccepted(count)
                        .setMessage("Published " + count + " event(s) successfully")
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    // -------------------------------------------------------------------------
    // Bidirectional-streaming RPC
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Full-duplex streaming: each event received from the client is published
     * to its target stream and immediately echoed back as the persisted
     * {@link DataEvent} (with server-assigned event ID and timestamp).
     */
    @Override
    public StreamObserver<DataEvent> streamBidirectional(
            StreamObserver<DataEvent> responseObserver) {

        return new StreamObserver<>() {
            @Override
            public void onNext(DataEvent protoEvent) {
                DataEventResponse saved = publishEventHandler.handle(new PublishEventCommand(
                        protoEvent.getStreamId(),
                        protoEvent.getPayload().toByteArray(),
                        protoEvent.getMetadataMap()));
                responseObserver.onNext(StreamProtoMapper.toProtoDataEvent(saved));
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
