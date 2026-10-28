---
tags: [ghost-serializer, retrofit, buffers, pooling]
source: ghost-retrofit/src/main/kotlin/com/ghost/serialization/retrofit/GhostRetrofitBuffers.kt
kind: object
---

# GhostRetrofitBuffers

An internal helper that reads a Retrofit response stream into a pooled buffer for Ghost's readers.

## What it is about

Every Retrofit converter factory in Ghost (JSON, proto3 JSON, YAML) starts the same way: read an `InputStream` into a byte array, parse it, and release the array. `GhostRetrofitBuffers.readToScratch` does this once, for all three. It is the Retrofit twin of the Ktor `GhostKtorBuffers`, adapted to blocking `InputStream` instead of a suspending channel.

## How it works

The function acquires a pooled scratch buffer from the core runtime (initial size `RESPONSE_SCRATCH_INITIAL_SIZE`) and loops: when the buffer is full it calls `growScratchBuffer`, which doubles it and keeps the used bytes; then `stream.read` fills the free part until it returns `-1`. The `block` is invoked with the buffer and its filled length, and a `finally` always returns the buffer to the pool.

The charset is the extra step. If `ghostInternalNeedsTranscoding` says the declared charset is not plain UTF-8 (the charset comes from the response `Content-Type`; `null` means assume UTF-8), the bytes are transcoded with `ghostInternalTranscodeToUtf8` and the block gets the new array. Ghost's readers only understand UTF-8, so this keeps non-UTF-8 responses correct while costing nothing for the common case.

The function is `inline`, so the lambda is inlined into the converter without an allocation for the closure.

## The code

```kotlin
if (ghostInternalNeedsTranscoding(charset = charset)) {
    val utf8 = ghostInternalTranscodeToUtf8(bytes = scratch, length = offset, charset = charset!!)
    return block(utf8, utf8.size)
}
return block(scratch, offset)
```

## Related

- [[GhostConverterFactory]]: uses it for JSON responses.
- [[GhostProtoConverterFactory]]: uses it for proto3 JSON responses.
- [[GhostYamlConverterFactory]]: uses it for YAML responses.
- [[GhostKtorBuffers]]: the Ktor version of the same loop.
- [[GhostBufferGrowth]]: the growth policy for the scratch buffer.
- [[GhostCharsetTranscoding.jvmAndroid]]: the transcoding helpers it calls.
- [[GhostPools]]: the pool that supplies and reclaims buffers.
