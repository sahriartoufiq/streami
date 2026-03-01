package com.datastream.infrastructure.streaming;

import com.datastream.application.dto.DataEventResponse;
import com.datastream.application.port.StreamEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of {@link StreamEventPublisher}.
 *
 * <p>Maintains a registry of subscriber queues keyed by stream ID. When an event
 * is published, it is placed into every registered queue for that stream so that
 * server-streaming gRPC subscribers can deliver it to their clients.
 *
 * <p>This implementation is thread-safe and suitable for single-node deployments.
 * A distributed implementation (e.g. backed by Redis pub/sub or a message broker)
 * would be needed for multi-node deployments.
 */
@Component
public class InMemoryStreamEventPublisher implements StreamEventPublisher {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<BlockingQueue<DataEventResponse>>>
            subscribers = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * <p>Offers the event to all subscriber queues registered for
     * {@code event.streamId()}. Queues that are full silently drop the event.
     */
    @Override
    public void publish(DataEventResponse event) {
        List<BlockingQueue<DataEventResponse>> queues = subscribers.get(event.streamId());
        if (queues != null) {
            for (BlockingQueue<DataEventResponse> queue : queues) {
                queue.offer(event);
            }
        }
    }

    /**
     * Registers a subscriber queue for the given stream.
     *
     * <p>The queue will receive every event published to the stream until
     * {@link #unsubscribe(String, BlockingQueue)} is called.
     *
     * @param streamId the stream ID to subscribe to; must not be null
     * @param queue    the queue that will receive published events; must not be null
     */
    public void subscribe(String streamId, BlockingQueue<DataEventResponse> queue) {
        subscribers.computeIfAbsent(streamId, k -> new CopyOnWriteArrayList<>()).add(queue);
    }

    /**
     * Removes a previously registered subscriber queue for the given stream.
     *
     * @param streamId the stream ID to unsubscribe from; must not be null
     * @param queue    the queue to remove; must not be null
     */
    public void unsubscribe(String streamId, BlockingQueue<DataEventResponse> queue) {
        CopyOnWriteArrayList<BlockingQueue<DataEventResponse>> queues = subscribers.get(streamId);
        if (queues != null) {
            queues.remove(queue);
        }
    }
}
