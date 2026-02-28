/**
 * Domain layer — the heart of the application.
 *
 * <p>Contains entities, value objects, domain services, repository interfaces,
 * domain events, and domain-specific exceptions. This layer has ZERO framework
 * dependencies (no Spring, no JPA annotations, no protobuf imports).
 *
 * <p>Sub-packages:
 * <ul>
 *   <li>{@code entity}      — Aggregate roots and domain entities</li>
 *   <li>{@code valueobject} — Immutable value objects (e.g. StreamId, UserId)</li>
 *   <li>{@code repository}  — Repository interfaces (implemented in infrastructure)</li>
 *   <li>{@code service}     — Domain services for cross-aggregate logic</li>
 *   <li>{@code event}       — Domain events published by aggregates</li>
 *   <li>{@code exception}   — Domain-specific exception types</li>
 * </ul>
 */
package com.datastream.domain;
