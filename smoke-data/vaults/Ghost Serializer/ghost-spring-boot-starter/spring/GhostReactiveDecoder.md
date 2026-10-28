---
tags: [ghost-serializer, spring-boot-starter, webflux, json, ndjson]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostReactiveDecoder.kt
kind: class
---

# GhostReactiveDecoder

The WebFlux `Decoder` that reads JSON and NDJSON request bodies into Ghost models.

## What it is about

In reactive Spring, the body arrives as a `Publisher<DataBuffer>` that may be split at arbitrary byte boundaries. This decoder turns that stream into model objects using Ghost, for both a single JSON document and for streams of newline-delimited JSON (NDJSON). It lives in `ghost-spring-boot-starter`.

## How it works

It extends `AbstractDecoder<Any>` for `application/json` and `application/x-ndjson`. `canDecode` additionally requires a Ghost serializer for the `ResolvableType`, so `List<T>`, `Set<T>` and `Map<K, V>` are unwrapped like in the other integrations.

`decode` chooses between two modes by checking the MIME type with `isNdJson`.

- **Joined** (`decodeJoined`, also used by `decodeToMono`): `DataBufferUtils.join` merges all buffers into one, which is turned into a byte array (releasing the buffer) and, if necessary, converted to UTF-8, then deserialized once.
- **Streaming** (`decodeStreaming`): NDJSON records do not line up with buffer boundaries, so the code re-frames the stream on `\n`. A `carry` array holds the incomplete tail of each buffer; complete lines are emitted, and the last unterminated line is flushed when the stream completes. Each line is then deserialized separately. Empty lines are skipped.

`deserializeBytes` picks the proto3-aware reader when `serializer.isProto` is true and otherwise calls `Ghost.deserialize`. Any exception is wrapped in a `GhostJsonException` with a "Failed to decode reactive buffer for" message.

## The code

```kotlin
if (combined[index] == GhostSpringMediaTypes.NDJSON_NEWLINE) {
    if (index > lineStart) lines += combined.copyOfRange(lineStart, index)
    lineStart = index + 1
}
// …
carry = combined.copyOfRange(lineStart, combined.size)
```

## Related

- [[GhostReactiveEncoder]]: the matching encoder.
- [[GhostSpringTypeSerializers]]: resolves the serializer from the element type.
- [[GhostSpringMediaTypes]]: NDJSON detection and newline byte.
- [[GhostSpringDataBuffers]]: `consumeToByteArray` and `asUtf8From`.
- [[GhostWebFluxAutoConfiguration]]: registers it.
- [[GhostJsonException]]: the exception it throws on failure.
- [[GhostYamlReactiveDecoder]]: the YAML sibling.
