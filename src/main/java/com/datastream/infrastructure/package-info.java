/**
 * Infrastructure layer — technical concerns and framework integrations.
 *
 * <p>Provides concrete implementations of domain repository interfaces,
 * Spring/JPA configuration, and gRPC interceptors for cross-cutting concerns
 * (e.g. exception mapping, authentication).
 *
 * <p>Sub-packages:
 * <ul>
 *   <li>{@code persistence}   — JPA entities, Spring Data repositories, JPA repository adapters</li>
 *   <li>{@code config}        — Spring configuration classes</li>
 *   <li>{@code interceptor}   — gRPC server interceptors (exception mapping, logging, etc.)</li>
 * </ul>
 */
package com.datastream.infrastructure;
