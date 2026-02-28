/**
 * Application layer — orchestrates use cases via CQRS-lite.
 *
 * <p>Coordinates domain objects to fulfill application use cases. Contains
 * commands, queries, command/query handlers, DTOs, and mappers. Depends on
 * the domain layer only; no infrastructure or framework concerns.
 *
 * <p>Sub-packages:
 * <ul>
 *   <li>{@code command} — Write-side command objects (e.g. CreateStreamCommand)</li>
 *   <li>{@code query}   — Read-side query objects (e.g. GetStreamQuery)</li>
 *   <li>{@code handler} — Command and query handlers (*Handler naming)</li>
 *   <li>{@code dto}     — Data Transfer Objects crossing layer boundaries</li>
 *   <li>{@code mapper}  — Mappers between domain objects and DTOs</li>
 * </ul>
 */
package com.datastream.application;
