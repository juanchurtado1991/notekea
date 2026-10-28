---
tags: [ghost-serializer, ktor, server, extensions]
source: ghost-ktor/src/jvmMain/kotlin/com/ghost/serialization/ktor/GhostServerExtensions.kt
kind: functions
---

# GhostServerExtensions

Extension functions on Ktor's `ApplicationCall` that respond with Ghost-serialized bodies, bypassing ContentNegotiation.

## What it is about

This is the server-side counterpart of [[GhostClientExtensions]]. When a Ktor route wants to send a model as JSON, proto3 JSON or YAML with a single call, it uses `respondGhost`, `respondGhostProto` or `respondGhostYaml`. The file is in the `jvmMain` source set of `ghost-ktor`, because Ktor Server is JVM only.

## How it works

All three are `suspend inline reified` and accept a `status` that defaults to `200 OK`.

- `respondGhost` finds the serializer for `T`, throws an `IllegalArgumentException` (using [[GhostKtorErrorMessages]]) if there is none, encodes with `Ghost.encodeToBytes` and responds with `ByteArrayContent` of type `application/json`.
- `respondGhostProto` calls `GhostProto.encodeToBytes` and also answers with `application/json`, since proto3 JSON is JSON. The proto3 rules are in the generated serializer itself.
- `respondGhostYaml` looks up the serializer, checks it is a `GhostYamlSerializer` (with a descriptive error otherwise), encodes with `ghostYamlEncodeToBytes` and responds as `application/yaml`.

Writing the bytes directly avoids the converter pipeline and its negotiation. The cost is that the content type is fixed by the function and the `Accept` header is ignored.

## The code

```kotlin
suspend inline fun <reified T : Any> ApplicationCall.respondGhost(
    value: T,
    status: HttpStatusCode = HttpStatusCode.OK
) {
    val serializer = Ghost.getSerializer(T::class)
        ?: throw IllegalArgumentException("${GhostKtorErrorMessages.SERIALIZER_NOT_FOUND_PREFIX}${T::class.simpleName}${GhostKtorErrorMessages.SERIALIZER_NOT_FOUND_SUFFIX}")

    val bytes = Ghost.encodeToBytes(serializer = serializer, value = value)
    respond(ByteArrayContent(bytes, ContentType.Application.Json, status))
}
```

## Related

- [[GhostClientExtensions]]: the client-side equivalents, `bodyGhost` and friends.
- [[GhostKtorErrorMessages]]: error text for missing serializers.
- [[GhostKtorMediaTypes]]: defines the `application/yaml` content type.
- [[Ghost]]: provides serializer lookup and byte encoding.
- [[GhostProto]]: encodes proto3 JSON.
- [[GhostYamlSerializer]]: required by the YAML response.
