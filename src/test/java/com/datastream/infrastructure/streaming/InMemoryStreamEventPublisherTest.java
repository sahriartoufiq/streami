package com.datastream.infrastructure.streaming;

import com.datastream.application.dto.DataEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InMemoryStreamEventPublisher}.
 */
class InMemoryStreamEventPublisherTest {

    private InMemoryStreamEventPublisher publisher;

    private static final String STREAM_ID = "stream-1";
    private static final String OTHER_STREAM_ID = "stream-2";

    @BeforeEach
    void setUp() {
        publisher = new InMemoryStreamEventPublisher();
    }

    // -------------------------------------------------------------------------
    // subscribe / publish
    // -------------------------------------------------------------------------

    @Test
    void should_DeliverEvent_When_SubscriberRegistered() throws InterruptedException {
        LinkedBlockingQueue<DataEventResponse> queue = new LinkedBlockingQueue<>();
        publisher.subscribe(STREAM_ID, queue);

        DataEventResponse event = eventFor(STREAM_ID);
        publisher.publish(event);

        DataEventResponse received = queue.poll(100, TimeUnit.MILLISECONDS);
        assertThat(received).isEqualTo(event);
    }

    @Test
    void should_DeliverEventToAllSubscribers_When_MultipleSubscribersRegistered()
            throws InterruptedException {
        LinkedBlockingQueue<DataEventResponse> q1 = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<DataEventResponse> q2 = new LinkedBlockingQueue<>();
        publisher.subscribe(STREAM_ID, q1);
        publisher.subscribe(STREAM_ID, q2);

        DataEventResponse event = eventFor(STREAM_ID);
        publisher.publish(event);

        assertThat(q1.poll(100, TimeUnit.MILLISECONDS)).isEqualTo(event);
        assertThat(q2.poll(100, TimeUnit.MILLISECONDS)).isEqualTo(event);
    }

    @Test
    void should_NotDeliverEvent_When_NoSubscriberRegistered() {
        DataEventResponse event = eventFor(STREAM_ID);

        // Publishing without any subscriber should not throw
        publisher.publish(event);
    }

    @Test
    void should_NotDeliverEventToOtherStream_When_DifferentStreamIdPublished()
            throws InterruptedException {
        LinkedBlockingQueue<DataEventResponse> queue = new LinkedBlockingQueue<>();
        publisher.subscribe(STREAM_ID, queue);

        publisher.publish(eventFor(OTHER_STREAM_ID));

        assertThat(queue.poll(50, TimeUnit.MILLISECONDS)).isNull();
    }

    // -------------------------------------------------------------------------
    // unsubscribe
    // -------------------------------------------------------------------------

    @Test
    void should_NotDeliverEvent_When_SubscriberUnregistered() throws InterruptedException {
        LinkedBlockingQueue<DataEventResponse> queue = new LinkedBlockingQueue<>();
        publisher.subscribe(STREAM_ID, queue);
        publisher.unsubscribe(STREAM_ID, queue);

        publisher.publish(eventFor(STREAM_ID));

        assertThat(queue.poll(50, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void should_OnlyDeliverToRemainingSubscribers_When_OneSubscriberUnregistered()
            throws InterruptedException {
        LinkedBlockingQueue<DataEventResponse> q1 = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<DataEventResponse> q2 = new LinkedBlockingQueue<>();
        publisher.subscribe(STREAM_ID, q1);
        publisher.subscribe(STREAM_ID, q2);
        publisher.unsubscribe(STREAM_ID, q1);

        DataEventResponse event = eventFor(STREAM_ID);
        publisher.publish(event);

        assertThat(q1.poll(50, TimeUnit.MILLISECONDS)).isNull();
        assertThat(q2.poll(100, TimeUnit.MILLISECONDS)).isEqualTo(event);
    }

    @Test
    void should_NotThrow_When_UnsubscribingFromUnknownStream() {
        LinkedBlockingQueue<DataEventResponse> queue = new LinkedBlockingQueue<>();
        // no exception expected
        publisher.unsubscribe("non-existent-stream", queue);
    }

    // -------------------------------------------------------------------------
    // multiple events
    // -------------------------------------------------------------------------

    @Test
    void should_DeliverAllEvents_When_MultipleEventsPublished() throws InterruptedException {
        LinkedBlockingQueue<DataEventResponse> queue = new LinkedBlockingQueue<>();
        publisher.subscribe(STREAM_ID, queue);

        DataEventResponse e1 = eventFor(STREAM_ID);
        DataEventResponse e2 = eventFor(STREAM_ID);
        DataEventResponse e3 = eventFor(STREAM_ID);
        publisher.publish(e1);
        publisher.publish(e2);
        publisher.publish(e3);

        assertThat(queue.poll(100, TimeUnit.MILLISECONDS)).isEqualTo(e1);
        assertThat(queue.poll(100, TimeUnit.MILLISECONDS)).isEqualTo(e2);
        assertThat(queue.poll(100, TimeUnit.MILLISECONDS)).isEqualTo(e3);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static DataEventResponse eventFor(String streamId) {
        return new DataEventResponse(
                java.util.UUID.randomUUID().toString(),
                streamId,
                new byte[]{1, 2, 3},
                Collections.emptyMap(),
                java.time.Instant.now().toString());
    }
}
