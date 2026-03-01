# DataStream gRPC API — Reference

Service: `datastream.v1.StreamService`
Default address: `localhost:9090`
Transport: gRPC over HTTP/2 (plaintext for local dev, TLS for production)

---

## Prerequisites

```bash
# macOS
brew install grpcurl

# Linux
curl -sSL https://github.com/fullstorydev/grpcurl/releases/latest/download/grpcurl_$(uname -s)_$(uname -m).tar.gz | tar -xz -C /usr/local/bin
```

Start the server locally:

```bash
docker compose up -d          # starts PostgreSQL
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Server reflection is enabled — use `grpcurl` to discover the API at runtime:

```bash
# List all services
grpcurl -plaintext localhost:9090 list

# Describe the StreamService
grpcurl -plaintext localhost:9090 describe datastream.v1.StreamService

# Describe any message type
grpcurl -plaintext localhost:9090 describe datastream.v1.CreateStreamRequest
```

---

## Enums

### StreamType
| Proto value | Meaning |
|---|---|
| `STREAM_TYPE_EVENT` | Discrete application events |
| `STREAM_TYPE_LOG` | Log lines / audit trail |
| `STREAM_TYPE_METRIC` | Time-series numeric metrics |
| `STREAM_TYPE_CUSTOM` | User-defined payload format |

### StreamStatus
| Proto value | Meaning |
|---|---|
| `STREAM_STATUS_DRAFT` | Created but not yet accepting events |
| `STREAM_STATUS_ACTIVE` | Accepting and delivering events |
| `STREAM_STATUS_INACTIVE` | Paused; not accepting events |
| `STREAM_STATUS_DELETED` | Soft-deleted; read-only |

### Valid status transitions
```
DRAFT ──► ACTIVE ──► INACTIVE ──► ACTIVE   (re-activate)
  │          │           │
  └──────────┴───────────┴──► DELETED
```

---

## Unary RPCs

### CreateStream

Creates a new stream in `DRAFT` status.

```bash
grpcurl -plaintext \
  -d '{
    "name": "sensor-events",
    "description": "IoT temperature readings",
    "owner_id": "00000000-0000-0000-0000-000000000001",
    "stream_type": "STREAM_TYPE_EVENT"
  }' \
  localhost:9090 datastream.v1.StreamService/CreateStream
```

<details>
<summary>Example response</summary>

```json
{
  "stream": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "sensor-events",
    "description": "IoT temperature readings",
    "owner_id": "00000000-0000-0000-0000-000000000001",
    "stream_type": "STREAM_TYPE_EVENT",
    "status": "STREAM_STATUS_DRAFT",
    "created_at": "2026-03-01T12:00:00Z",
    "updated_at": "2026-03-01T12:00:00Z"
  }
}
```
</details>

**Error codes**
| Code | Condition |
|---|---|
| `ALREADY_EXISTS` | A stream with this name already exists |
| `INVALID_ARGUMENT` | Invalid `stream_type` or malformed `owner_id` UUID |

---

### GetStream

Retrieves a single stream by ID.

```bash
grpcurl -plaintext \
  -d '{"id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"}' \
  localhost:9090 datastream.v1.StreamService/GetStream
```

<details>
<summary>Example response</summary>

```json
{
  "stream": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "sensor-events",
    "description": "IoT temperature readings",
    "owner_id": "00000000-0000-0000-0000-000000000001",
    "stream_type": "STREAM_TYPE_EVENT",
    "status": "STREAM_STATUS_DRAFT",
    "created_at": "2026-03-01T12:00:00Z",
    "updated_at": "2026-03-01T12:00:00Z"
  }
}
```
</details>

**Error codes**
| Code | Condition |
|---|---|
| `NOT_FOUND` | No stream with the given ID |

---

### ListStreams

Returns a paginated, optionally filtered list of streams.

```bash
# All streams (page 0, 20 per page)
grpcurl -plaintext \
  -d '{"page": 0, "size": 20}' \
  localhost:9090 datastream.v1.StreamService/ListStreams

# Filter by owner
grpcurl -plaintext \
  -d '{
    "filter": {"owner_id": "00000000-0000-0000-0000-000000000001"},
    "page": 0,
    "size": 10
  }' \
  localhost:9090 datastream.v1.StreamService/ListStreams

# Filter by status
grpcurl -plaintext \
  -d '{
    "filter": {"status": "STREAM_STATUS_ACTIVE"},
    "page": 0,
    "size": 10
  }' \
  localhost:9090 datastream.v1.StreamService/ListStreams

# Filter by stream type
grpcurl -plaintext \
  -d '{
    "filter": {"stream_type": "STREAM_TYPE_METRIC"},
    "page": 0,
    "size": 10
  }' \
  localhost:9090 datastream.v1.StreamService/ListStreams

# Combined filters
grpcurl -plaintext \
  -d '{
    "filter": {
      "owner_id": "00000000-0000-0000-0000-000000000001",
      "status": "STREAM_STATUS_ACTIVE",
      "stream_type": "STREAM_TYPE_EVENT"
    },
    "page": 0,
    "size": 5
  }' \
  localhost:9090 datastream.v1.StreamService/ListStreams
```

<details>
<summary>Example response</summary>

```json
{
  "streams": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "name": "sensor-events",
      "owner_id": "00000000-0000-0000-0000-000000000001",
      "stream_type": "STREAM_TYPE_EVENT",
      "status": "STREAM_STATUS_ACTIVE",
      "created_at": "2026-03-01T12:00:00Z",
      "updated_at": "2026-03-01T12:01:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "total_elements": "1",
    "total_pages": 1
  }
}
```
</details>

---

### UpdateStream

Partial update — only fields present in the request are applied.
Supports config updates (`name`, `description`) and lifecycle transitions (`status`) in a single call.

```bash
# Rename a stream
grpcurl -plaintext \
  -d '{
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "iot-sensor-events",
    "description": "Updated description"
  }' \
  localhost:9090 datastream.v1.StreamService/UpdateStream

# Activate (DRAFT or INACTIVE → ACTIVE)
grpcurl -plaintext \
  -d '{
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "STREAM_STATUS_ACTIVE"
  }' \
  localhost:9090 datastream.v1.StreamService/UpdateStream

# Deactivate (ACTIVE → INACTIVE)
grpcurl -plaintext \
  -d '{
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "STREAM_STATUS_INACTIVE"
  }' \
  localhost:9090 datastream.v1.StreamService/UpdateStream

# Rename and activate in one call
grpcurl -plaintext \
  -d '{
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "live-sensor-events",
    "status": "STREAM_STATUS_ACTIVE"
  }' \
  localhost:9090 datastream.v1.StreamService/UpdateStream
```

**Error codes**
| Code | Condition |
|---|---|
| `NOT_FOUND` | No stream with the given ID |
| `FAILED_PRECONDITION` | Transition not allowed from current status (e.g. deactivating a DRAFT) |
| `INVALID_ARGUMENT` | Unknown target status value |

---

### DeleteStream

Soft-deletes a stream (transitions to `DELETED`). The record is retained in the database.

```bash
grpcurl -plaintext \
  -d '{"id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"}' \
  localhost:9090 datastream.v1.StreamService/DeleteStream
```

<details>
<summary>Example response</summary>

```json
{
  "success": true,
  "message": "Stream deleted successfully"
}
```
</details>

**Error codes**
| Code | Condition |
|---|---|
| `NOT_FOUND` | No stream with the given ID |
| `FAILED_PRECONDITION` | Stream is already in `DELETED` status |

---

## Server-Streaming RPC

### SubscribeToStream

Opens a persistent server-streaming connection. The server pushes each `DataEvent` in real time as events are published to the stream. The stream remains open until the client cancels.

```bash
# Subscribe and receive live events (Ctrl+C to cancel)
grpcurl -plaintext \
  -d '{"stream_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"}' \
  localhost:9090 datastream.v1.StreamService/SubscribeToStream
```

Each delivered message looks like:

```json
{
  "event_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "stream_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "payload": "eyJzZW5zb3IiOiJTMDEiLCJ0ZW1wIjoyMy41fQ==",
  "metadata": {
    "source": "sensor-01",
    "region": "eu-west-1"
  },
  "timestamp": "2026-03-01T12:05:00.123456789Z"
}
```

> **Note:** `payload` is base64-encoded binary. Decode with `base64 -d`.

**Error codes**
| Code | Condition |
|---|---|
| `NOT_FOUND` | No stream with the given ID |

---

## Client-Streaming RPC

### PublishToStream

Client streams a batch of events. The server processes each one and returns a summary when the client closes the stream.

> `grpcurl` does not support interactive client-streaming. Use the batch file approach below.

```bash
# Write all requests to a newline-delimited JSON file
cat > /tmp/publish_batch.json << 'EOF'
{"stream_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "payload": "eyJ0ZW1wIjoyMy41fQ==", "metadata": {"source": "sensor-01"}}
{"stream_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "payload": "eyJ0ZW1wIjoyNC4xfQ==", "metadata": {"source": "sensor-01"}}
{"stream_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "payload": "eyJ0ZW1wIjoyMi44fQ==", "metadata": {"source": "sensor-02"}}
EOF

grpcurl -plaintext \
  -d @ \
  localhost:9090 datastream.v1.StreamService/PublishToStream < /tmp/publish_batch.json
```

<details>
<summary>Example response</summary>

```json
{
  "events_accepted": "3",
  "message": "Published 3 event(s) successfully"
}
```
</details>

**Error codes**
| Code | Condition |
|---|---|
| `NOT_FOUND` | `stream_id` does not exist |
| `FAILED_PRECONDITION` | Target stream is not `ACTIVE` |

---

## Bidirectional-Streaming RPC

### StreamBidirectional

Full-duplex stream: client sends `DataEvent` messages, the server publishes each one and immediately echoes back the persisted event (with server-assigned `event_id` and `timestamp`).

```bash
cat > /tmp/bidi_events.json << 'EOF'
{"stream_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "payload": "eyJtc2ciOiJoZWxsbyJ9", "metadata": {}}
{"stream_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "payload": "eyJtc2ciOiJ3b3JsZCJ9", "metadata": {}}
EOF

grpcurl -plaintext \
  -d @ \
  localhost:9090 datastream.v1.StreamService/StreamBidirectional < /tmp/bidi_events.json
```

Each echoed response carries the server-assigned fields:

```json
{
  "event_id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "stream_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "payload": "eyJtc2ciOiJoZWxsbyJ9",
  "metadata": {},
  "timestamp": "2026-03-01T12:10:00.000000001Z"
}
```

---

## End-to-End Workflow Example

```bash
STREAM_ID=""
OWNER="00000000-0000-0000-0000-000000000001"

# 1. Create a stream
STREAM_ID=$(grpcurl -plaintext \
  -d "{\"name\":\"demo-stream\",\"owner_id\":\"$OWNER\",\"stream_type\":\"STREAM_TYPE_EVENT\"}" \
  localhost:9090 datastream.v1.StreamService/CreateStream \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['stream']['id'])")

echo "Created stream: $STREAM_ID"

# 2. Activate it
grpcurl -plaintext \
  -d "{\"id\":\"$STREAM_ID\",\"status\":\"STREAM_STATUS_ACTIVE\"}" \
  localhost:9090 datastream.v1.StreamService/UpdateStream

# 3. Subscribe in the background (will print events as they arrive)
grpcurl -plaintext \
  -d "{\"stream_id\":\"$STREAM_ID\"}" \
  localhost:9090 datastream.v1.StreamService/SubscribeToStream &
SUBSCRIBE_PID=$!

sleep 0.5

# 4. Publish a batch of events
cat > /tmp/events.json << EOF
{"stream_id": "$STREAM_ID", "payload": "$(echo -n '{"temp":23.5}' | base64)", "metadata": {"source":"sensor-01"}}
{"stream_id": "$STREAM_ID", "payload": "$(echo -n '{"temp":24.1}' | base64)", "metadata": {"source":"sensor-01"}}
EOF

grpcurl -plaintext -d @ \
  localhost:9090 datastream.v1.StreamService/PublishToStream < /tmp/events.json

sleep 1

# 5. List active streams
grpcurl -plaintext \
  -d '{"filter":{"status":"STREAM_STATUS_ACTIVE"},"page":0,"size":10}' \
  localhost:9090 datastream.v1.StreamService/ListStreams

# 6. Clean up
kill $SUBSCRIBE_PID 2>/dev/null
grpcurl -plaintext \
  -d "{\"id\":\"$STREAM_ID\"}" \
  localhost:9090 datastream.v1.StreamService/DeleteStream
```

---

## Error Reference

All errors follow the standard [gRPC status codes](https://grpc.github.io/grpc/core/md_doc_statuscodes.html).

| gRPC Status | HTTP equivalent | When it occurs |
|---|---|---|
| `NOT_FOUND` | 404 | Stream ID does not exist |
| `ALREADY_EXISTS` | 409 | Stream name is already taken |
| `FAILED_PRECONDITION` | 400 | Invalid state transition or operation on a deleted stream |
| `INVALID_ARGUMENT` | 400 | Malformed UUID, unknown enum value |
| `INTERNAL` | 500 | Unexpected server error |

---

## Proto Source

The full service definition lives at:

```
src/main/proto/datastream/v1/stream_service.proto
```

Generated Java stubs are compiled to `com.datastream.interfaces.grpc.proto.*` via the `protobuf-maven-plugin` during `mvn compile`.
