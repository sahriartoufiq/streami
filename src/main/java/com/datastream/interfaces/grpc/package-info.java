/**
 * Interfaces layer — gRPC service adapters.
 *
 * <p>Contains thin gRPC service implementations that adapt protobuf requests
 * to application layer commands/queries and translate responses back to
 * protobuf messages. No business logic lives here.
 *
 * <p>Naming convention: {@code *GrpcService} for service implementations,
 * {@code *ProtoMapper} for protobuf ↔ DTO mapping.
 */
package com.datastream.interfaces.grpc;
