---
tags: [ghost-serializer, spring-boot-starter, buffers, charset]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostSpringDataBuffers.kt
kind: functions
---

# GhostSpringDataBuffers

Two small internal extension functions for turning Spring buffers into the UTF-8 byte arrays that Ghost readers expect.

## What it is about

Ghost's parsers read byte arrays in UTF-8. Spring offers `DataBuffer` objects in reactive code and arbitrary charsets in headers. This file contains the glue, shared by the MVC converters and the reactive decoders of `ghost-spring-boot-starter`.

## How it works

`DataBuffer.consumeToByteArray()` allocates an array with `readableByteCount()`, reads the data into it and, in a `finally` block, releases the buffer with `DataBufferUtils.release`. Releasing is mandatory in Spring's reactive model: every decoder owns the buffers it receives, and forgetting it would leak pooled memory. Doing it in `finally` covers errors as well.

`ByteArray.asUtf8From(charset)` returns the same array when the charset is `null` or does not need transcoding. If `ghostInternalNeedsTranscoding` says the charset differs from UTF-8, it calls `ghostInternalTranscodeToUtf8` and returns the converted array. The common UTF-8 case therefore costs nothing; only unusual charsets pay for a copy.

Both are `internal`, and the second opts in to `InternalGhostApi` because the transcoding helpers are internal Ghost API.

## The code

```kotlin
@OptIn(InternalGhostApi::class)
internal fun ByteArray.asUtf8From(
    charset: Charset?
): ByteArray = if (charset != null && ghostInternalNeedsTranscoding(charset = charset)) {
    ghostInternalTranscodeToUtf8(
        bytes = this,
        length = size,
        charset = charset
    )
} else {
    this
}
```

## Related

- [[GhostReactiveDecoder]]: calls both functions when joining buffers.
- [[GhostYamlReactiveDecoder]]: calls both for YAML.
- [[GhostHttpMessageConverter]]: uses `asUtf8From` on request bodies.
- [[GhostYamlHttpMessageConverter]]: uses it for YAML request bodies.
- [[GhostCharsetTranscoding.jvmAndroid]]: provides the transcoding helpers.
- [[InternalGhostApi]]: the opt-in required to call them.
