package com.datastream.application.port;

import com.datastream.application.dto.DataEventResponse;

/**
 * Output port for notifying subscribers of newly published data events.
 *
 * <p>Defined in the application layer; implemented in the infrastructure layer
 * (e.g. via an in-memory fanout, WebSocket push, or message broker adapter).
 */
public interface StreamEventPublisher {

    /**
     * Publishes a data event notification to all active subscribers of the stream.
     *
     * @param event the event that was published; must not be null
     */
    void publish(DataEventResponse event);
}
