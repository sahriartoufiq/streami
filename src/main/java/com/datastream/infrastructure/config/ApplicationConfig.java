package com.datastream.infrastructure.config;

import com.datastream.application.handler.ActivateStreamHandler;
import com.datastream.application.handler.CreateStreamHandler;
import com.datastream.application.handler.DeactivateStreamHandler;
import com.datastream.application.handler.DeleteStreamHandler;
import com.datastream.application.handler.GetStreamHandler;
import com.datastream.application.handler.ListStreamsHandler;
import com.datastream.application.handler.PublishEventHandler;
import com.datastream.application.handler.UpdateStreamHandler;
import com.datastream.application.port.StreamEventPublisher;
import com.datastream.domain.repository.DataEventRepository;
import com.datastream.domain.repository.StreamRepository;
import com.datastream.domain.service.StreamDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires application-layer handlers and domain services.
 *
 * <p>Application handlers have no Spring annotations themselves (keeping the
 * application layer free of framework dependencies), so they are registered
 * here as Spring beans via factory methods.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Provides the {@link StreamDomainService} bean.
     *
     * @param streamRepository the stream repository; injected by Spring
     * @return the configured domain service
     */
    @Bean
    public StreamDomainService streamDomainService(StreamRepository streamRepository) {
        return new StreamDomainService(streamRepository);
    }

    /**
     * Provides the {@link CreateStreamHandler} bean.
     *
     * @param streamDomainService domain service for name uniqueness validation
     * @param streamRepository    repository for persisting the new stream
     * @return the configured handler
     */
    @Bean
    public CreateStreamHandler createStreamHandler(
            StreamDomainService streamDomainService,
            StreamRepository streamRepository) {
        return new CreateStreamHandler(streamDomainService, streamRepository);
    }

    /**
     * Provides the {@link ActivateStreamHandler} bean.
     *
     * @param streamDomainService domain service for loading the stream
     * @param streamRepository    repository for persisting the activated stream
     * @return the configured handler
     */
    @Bean
    public ActivateStreamHandler activateStreamHandler(
            StreamDomainService streamDomainService,
            StreamRepository streamRepository) {
        return new ActivateStreamHandler(streamDomainService, streamRepository);
    }

    /**
     * Provides the {@link DeactivateStreamHandler} bean.
     *
     * @param streamDomainService domain service for loading the stream
     * @param streamRepository    repository for persisting the deactivated stream
     * @return the configured handler
     */
    @Bean
    public DeactivateStreamHandler deactivateStreamHandler(
            StreamDomainService streamDomainService,
            StreamRepository streamRepository) {
        return new DeactivateStreamHandler(streamDomainService, streamRepository);
    }

    /**
     * Provides the {@link UpdateStreamHandler} bean.
     *
     * @param streamDomainService domain service for loading the stream
     * @param streamRepository    repository for persisting the updated stream
     * @return the configured handler
     */
    @Bean
    public UpdateStreamHandler updateStreamHandler(
            StreamDomainService streamDomainService,
            StreamRepository streamRepository) {
        return new UpdateStreamHandler(streamDomainService, streamRepository);
    }

    /**
     * Provides the {@link DeleteStreamHandler} bean.
     *
     * @param streamDomainService domain service for loading the stream
     * @param streamRepository    repository for persisting the deleted stream
     * @return the configured handler
     */
    @Bean
    public DeleteStreamHandler deleteStreamHandler(
            StreamDomainService streamDomainService,
            StreamRepository streamRepository) {
        return new DeleteStreamHandler(streamDomainService, streamRepository);
    }

    /**
     * Provides the {@link PublishEventHandler} bean.
     *
     * @param streamDomainService    domain service for validating stream state
     * @param dataEventRepository    repository for persisting events
     * @param streamEventPublisher   publisher for notifying live subscribers
     * @return the configured handler
     */
    @Bean
    public PublishEventHandler publishEventHandler(
            StreamDomainService streamDomainService,
            DataEventRepository dataEventRepository,
            StreamEventPublisher streamEventPublisher) {
        return new PublishEventHandler(streamDomainService, dataEventRepository, streamEventPublisher);
    }

    /**
     * Provides the {@link GetStreamHandler} bean.
     *
     * @param streamRepository repository for reading stream data
     * @return the configured handler
     */
    @Bean
    public GetStreamHandler getStreamHandler(StreamRepository streamRepository) {
        return new GetStreamHandler(streamRepository);
    }

    /**
     * Provides the {@link ListStreamsHandler} bean.
     *
     * @param streamRepository repository for reading stream data
     * @return the configured handler
     */
    @Bean
    public ListStreamsHandler listStreamsHandler(StreamRepository streamRepository) {
        return new ListStreamsHandler(streamRepository);
    }
}
